package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.Abg;
import com.ventsim.ventsim.model.PatientState;

public final class AbgCalculator {
    private AbgCalculator() {}

    // Constants
    private static final double PB = 760.0;    // mmHg
    private static final double PH2O = 47.0;   // mmHg
    private static final double RQ = 0.8;
    private static final double VCO2 = 200.0;  // mL/min

    // Hill-type saturation
    private static double sao2FromPaO2(double paO2) {
        double x3 = Math.pow(paO2, 3.0);
        double k3 = Math.pow(150.0, 3.0);
        double sat = 100.0 * x3 / (x3 + k3);
        return Units.clamp(sat, 50, 100);
    }

    public static Abg initAbgOrDefault(Abg abg, String scenario) {
        if (abg != null) return abg.clamp();
        return switch (scenario.toLowerCase()) {
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
        // Siggaard-Andersen approximation
        return 0.93*(hco3 - 24.4 + 14.83*(pH - 7.4));
    }

    private static double shuntEff(double base, double peep) {
        return Math.max(0.02, base - 0.015*peep);
    }

    private static double paO2FromPAO2(double PAO2, double shuntEff) {
        return Units.clamp(PAO2 * (1.0 - shuntEff), 30, 600);
    }

    public static Abg nextAbg(PatientState s, double newVt, int newRr, double newPeep, double newFiO2) {
        // Baselines by scenario
        double baseVDVT = switch (s.scenario()) {
            case NORMAL -> 0.30;
            case COPD   -> 0.50;
            case ARDS   -> 0.40;
            case ASTHMA -> 0.55;
        };

        double vdvtNow  = vdvt(baseVDVT, newRr, newPeep, s.resistance());
        double trap     = trapFrac(s.resistance(), s.compliance(), newRr);
        double VAnew    = alveolarVentLpm(newVt, newRr, vdvtNow, trap);

        // Use previous state's effective VA to compute proportional PaCO2 change
        double vdvtPrev = vdvt(baseVDVT, s.rr(), s.peep(), s.resistance());
        double trapPrev = trapFrac(s.resistance(), s.compliance(), s.rr());
        double VAprev   = alveolarVentLpm(s.vtMl(), s.rr(), vdvtPrev, trapPrev);

        double paCO2_prev = s.abg().paCO2();
        double paCO2_new  = Units.clamp(paCO2_prev * (VAprev / VAnew), 20, 120);

        // HCO3 compensation (slower for non-COPD)
        double comp = (s.scenario() == com.ventsim.ventsim.model.Scenario.COPD) ? 0.35 : 0.10;
        double hco3_new = Units.clamp(s.abg().hco3() + comp*(paCO2_new - paCO2_prev), 5, 60);

        // Oxygenation via alveolar gas equation & shunt
        double PAO2 = newFiO2 * (PB - PH2O) - (paCO2_new / RQ);
        double paO2_new = paO2FromPAO2(PAO2, shuntEff(s.shuntBase(), newPeep));

        double pH_new = Units.clamp(pHFromHH(paCO2_new, hco3_new), 6.5, 7.8);
        double saO2_new = sao2FromPaO2(paO2_new);
        double be_new = baseExcess(pH_new, hco3_new);

        return new Abg(pH_new, paCO2_new, paO2_new, hco3_new, saO2_new, be_new).clamp();
    }
}
