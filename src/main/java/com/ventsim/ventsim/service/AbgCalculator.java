package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.Abg;
import com.ventsim.ventsim.model.Mode;
import com.ventsim.ventsim.model.PatientState;
import com.ventsim.ventsim.model.Scenario;

public final class AbgCalculator {
    private AbgCalculator() {}

    // Constants
    private static final double PB = 760.0;    // mmHg
    private static final double PH2O = 47.0;   // mmHg
    private static final double RQ = 0.8;
    private static final double VCO2 = 200.0;  // mL/min

    // For result wrapping (ABG + fatal flag)
    public static final class AbgResult {
        public final Abg abg;
        public final boolean fatal;
        AbgResult(Abg abg, boolean fatal) { this.abg = abg; this.fatal = fatal; }
    }

    // Hill-type saturation
    private static double sao2FromPaO2(double paO2) {
        double x3 = Math.pow(paO2, 3.0);
        double k3 = Math.pow(150.0, 3.0);
        double sat = 100.0 * x3 / (x3 + k3);
        return Units.clamp(sat, 50, 100);
    }

    public static Abg initAbgOrDefault(Abg abg, String scenario) {
        if (abg != null) return abg.clamp();
        return switch (scenario == null ? "normal" : scenario.toLowerCase()) {
            case "copd"   -> new Abg(7.34, 52, 65, 28, 90, 0);
            case "ards"   -> new Abg(7.32, 45, 55, 24, 88, -2);
            case "asthma" -> new Abg(7.30, 58, 60, 24, 90, -3);
            default       -> new Abg(7.40, 40, 95, 24, 97, 0);
        };
    }

    public static double[] vtTargetRange(double weightKg) {
        if (weightKg <= 0) return new double[]{500, 500}; // default if missing
        return new double[]{weightKg * 6.0, weightKg * 8.0};
    }

    /** Compute Vt (mL) based on mode and inputs.
     *  - VC: use provided vtMlRequested (min clamps applied).
     *  - PC: Vt ≈ (PinspDelta above PEEP) * compliance (mL/cmH2O) → mL
     *  - PSV: Vt ≈ supportPressure * compliance; bounded by 5–10 mL/kg if weight known.
     *  Notes:
     *   * inspiratoryPressure is interpreted as **delta above PEEP** (common in PCV).
     *   * supportPressure is PS above PEEP (PSV).
     */
    public static double computeVtMlFromMode(
            Mode mode,
            double vtMlRequested,
            Double inspiratoryPressure,
            Double supportPressure,
            double peep,
            double compliance_ml_per_cmH2O,
            double weightKg
    ) {
        double vt;
        switch (mode) {
            case PC -> {
                double pinspDelta = (inspiratoryPressure == null) ? 0.0 : Math.max(0.0, inspiratoryPressure);
                vt = pinspDelta * compliance_ml_per_cmH2O; // mL
            }
            case PSV -> {
                double ps = (supportPressure == null) ? 0.0 : Math.max(0.0, supportPressure);
                vt = ps * compliance_ml_per_cmH2O; // mL
            }
            default -> {
                // VC: honor requested Vt
                vt = Math.max(100.0, vtMlRequested);
            }
        }
        // Safety & physiological guidance clamps
        if (weightKg > 0) {
            double min = weightKg * 5.0;  // permissive floor
            double max = weightKg * 10.0; // ceiling
            vt = Math.max(min, Math.min(max, vt));
        } else {
            vt = Math.max(300.0, Math.min(800.0, vt)); // generic bounds without weight
        }
        return vt;
    }

    private static double vdvt(double base, int rr, double peep, double resistance) {
        double v = base + 0.002*(rr-12) + 0.001*(resistance-10) - 0.003*(peep-5);
        return Math.max(0.2, Math.min(0.7, v));
    }

    private static double trapFrac(double R, double Cml, int rr) {
        double ttot = 60.0 / Math.max(6, rr);
        double te = (2.0/3.0) * ttot;
        double tau = R * (Cml / 1000.0); // seconds
        if (3.0 * tau <= te) return 0.0;
        double f = (3.0 * tau - te) / (3.0 * tau);
        return Math.max(0.0, Math.min(0.30, f));
    }

    private static double alveolarVentLpm(double vtMl, int rr, double vdvt, double trap) {
        double va_t = Math.max(50.0, vtMl * (1.0 - vdvt)) * (1.0 - trap);
        return Math.max(0.4, (va_t / 1000.0) * rr);
    }

    private static double paCO2FromVA(double VA) {
        return Units.clamp(0.863 * VCO2 / VA, 20, 120);
    }

    private static double pHFromHH(double paCO2, double hco3) {
        return 6.1 + Math.log10(hco3 / (0.03 * paCO2));
    }

    private static double baseExcess(double pH, double hco3) {
        return 0.93*(hco3 - 24.4 + 14.83*(pH - 7.4));
    }

    private static double shuntEff(double base, double peep) {
        return Math.max(0.02, base - 0.015*peep);
    }

    private static double paO2FromPAO2(double PAO2, double shuntEff) {
        // raw PAO2 component can be negative at extreme hypercapnia; treat as unrealistic fatal
        return PAO2 * (1.0 - shuntEff);
    }

    /** Compute next ABG based on new settings. Returns ABG + fatal flag (pre-clamp). */
    public static AbgResult nextAbg(
            PatientState s,
            double newVt,
            int newRr,
            double newPeep,
            double newFiO2
    ) {
        double baseVDVT = switch (s.scenario()) {
            case NORMAL -> 0.30;
            case COPD   -> 0.50;
            case ARDS   -> 0.40;
            case ASTHMA -> 0.55;
        };

        double vdvtNow  = vdvt(baseVDVT, newRr, newPeep, s.resistance());
        double trap     = trapFrac(s.resistance(), s.compliance(), newRr);
        double VAnew    = alveolarVentLpm(newVt, newRr, vdvtNow, trap);

        double vdvtPrev = vdvt(baseVDVT, s.rr(), s.peep(), s.resistance());
        double trapPrev = trapFrac(s.resistance(), s.compliance(), s.rr());
        double VAprev   = alveolarVentLpm(s.vtMl(), s.rr(), vdvtPrev, trapPrev);

        double paCO2_prev = s.abg().paCO2();
        double paCO2_raw  = paCO2_prev * (VAprev / VAnew);

        // Compensation (slower if not COPD)
        double comp = (s.scenario() == Scenario.COPD) ? 0.35 : 0.10;
        double hco3_raw = s.abg().hco3() + comp*(paCO2_raw - paCO2_prev);

        double PAO2_raw = newFiO2 * (PB - PH2O) - (paCO2_raw / RQ);
        double paO2_raw = paO2FromPAO2(PAO2_raw, shuntEff(s.shuntBase(), newPeep));

        double pH_raw   = pHFromHH(paCO2_raw, hco3_raw);
        double saO2_raw = sao2FromPaO2(paO2_raw);
        double be_raw   = baseExcess(pH_raw, hco3_raw);

        // Fatal BEFORE clamp (catch unrealistic physiology)
        boolean fatal =
                (pH_raw < 6.5 || pH_raw > 7.8) ||
                        (paCO2_raw < 15 || paCO2_raw > 120) ||
                        (paO2_raw < 30 || paO2_raw > 600) || // negative PAO2/PaO2 gets caught here
                        (hco3_raw < 5  || hco3_raw > 60)  ||
                        (saO2_raw < 50 || saO2_raw > 100);

        // Now clamp to safe output range
        Abg abg = new Abg(
                pH_raw,
                paCO2_raw,
                paO2_raw,
                hco3_raw,
                saO2_raw,
                be_raw
        ).clamp();

        return new AbgResult(abg, fatal);
    }
}
