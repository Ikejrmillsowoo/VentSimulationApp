package com.ventsim.ventsim.model;

public record Abg (
        double pH,
        double paCO2, // mmHg
        double paO2,  // mmHg
        double hco3,  // mEq/L
        double saO2,  // %
        double be     // base excess
) {
    public Abg clamp() {
        double pHc   = Math.max(6.5, Math.min(7.8, pH));
        double pCO2c = Math.max(15, Math.min(120, paCO2));
        double pO2c  = Math.max(30, Math.min(600, paO2));
        double hco3c = Math.max(5,  Math.min(60, hco3));
        double sao2c = Math.max(50, Math.min(100, saO2));
        double bec   = Math.max(-30,Math.min(30, be));
        return new Abg(pHc, pCO2c, pO2c, hco3c, sao2c, bec);
    }

}
