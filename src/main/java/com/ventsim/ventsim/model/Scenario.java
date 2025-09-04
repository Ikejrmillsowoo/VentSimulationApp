package com.ventsim.ventsim.model;

public enum Scenario {
    NORMAL, COPD, ARDS, ASTHMA;

    public static Scenario parse(String s) {
        if (s == null) return NORMAL;
        return switch (s.trim().toLowerCase()) {
            case "normal" -> NORMAL;
            case "copd"   -> COPD;
            case "ards"   -> ARDS;
            case "asthma" -> ASTHMA;
            default -> NORMAL;
        };
    }
}
