package com.ventsim.ventsim.model;

import java.util.UUID;

public class PatientState {
    private final String id = UUID.randomUUID().toString();
    private final Scenario scenario;
    private final Mode mode;
    private final double weightKg;           // if 0 → unknown
    private double vtMl;
    private int rr;
    private double peep;
    private double fio2Frac;                 // 0.21–1.0
    private Double inspPressure;             // nullable
    private Double supportPressure;          // nullable
    private Abg abg;

    // disease mechanics snapshot
    private final double compliance;         // mL/cmH2O
    private final double resistance;         // cmH2O/L/s
    private final double shuntBase;          // 0–1

    public PatientState(Scenario scenario, Mode mode, double weightKg,
                        double vtMl, int rr, double peep, double fio2Frac,
                        Double inspPressure, Double supportPressure,
                        Abg abg, double compliance, double resistance, double shuntBase) {
        this.scenario = scenario;
        this.mode = mode;
        this.weightKg = weightKg;
        this.vtMl = vtMl;
        this.rr = rr;
        this.peep = peep;
        this.fio2Frac = fio2Frac;
        this.inspPressure = inspPressure;
        this.supportPressure = supportPressure;
        this.abg = abg;
        this.compliance = compliance;
        this.resistance = resistance;
        this.shuntBase = shuntBase;
    }

    public String id() { return id; }
    public Scenario scenario() { return scenario; }
    public Mode mode() { return mode; }
    public double weightKg() { return weightKg; }
    public double vtMl() { return vtMl; }
    public int rr() { return rr; }
    public double peep() { return peep; }
    public double fio2Frac() { return fio2Frac; }
    public Double inspPressure() { return inspPressure; }
    public Double supportPressure() { return supportPressure; }
    public Abg abg() { return abg; }
    public double compliance() { return compliance; }
    public double resistance() { return resistance; }
    public double shuntBase() { return shuntBase; }

    public void setVent(double vtMl, int rr, double peep, double fio2Frac,
                        Double inspPressure, Double supportPressure) {
        this.vtMl = vtMl; this.rr = rr; this.peep = peep;
        this.fio2Frac = fio2Frac; this.inspPressure = inspPressure; this.supportPressure = supportPressure;
    }
    public void setAbg(Abg abg) { this.abg = abg; }
}
