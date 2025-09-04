package com.ventsim.ventsim.model;

public enum Mode {
    VC, PC, PSV;

    public static Mode parse(String s) {
        if (s == null) return Mode.VC;
        String t = s.replaceAll("\\s+", "").toLowerCase();
        return switch (t) {
            case "volumecontrol", "vc" -> VC;
            case "pressurecontrol", "pc" -> PC;
            case "psv", "pressuresupport" -> PSV;
            default -> VC;
        };
    }
}
