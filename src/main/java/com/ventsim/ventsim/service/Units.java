package com.ventsim.ventsim.service;

public final class Units {
    private Units() {}
    public static double fio2ToFrac(int fio2) { // handles 21..100 and 0..1 forms
        if (fio2 <= 1) return clamp(fio2, 0.21, 1.0);
        return clamp(fio2 / 100.0, 0.21, 1.0);
    }
    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
