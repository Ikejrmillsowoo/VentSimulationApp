package com.ventsim.ventsim.model;

public enum Scenario {
    NORMAL, COPD, ARDS, ASTHMA, PE, SEDATION, NEUROMUSCULAR_DISEASE;

    public static Scenario parse(String s) {
        if (s == null) return NORMAL;
        return switch (s.trim().toLowerCase()) {
            case "normal" -> NORMAL;
            case "copd"   -> COPD;
            case "ards"   -> ARDS;
            case "asthma" -> ASTHMA;
            case "pulmonary embolism", "pe" -> PE;
            case "sedation" -> SEDATION;
            case "neuromuscular disease", "nmd" -> NEUROMUSCULAR_DISEASE;
            default -> NORMAL;
        };
    }
}
