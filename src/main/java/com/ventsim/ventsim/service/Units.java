package com.ventsim.ventsim.service;

public final class Units {
    private Units() {}
    public static double fio2ToFrac(int fio2) { // 21..100 or 0..1
        if (fio2 <= 1) return clamp(fio2, 0.21, 1.0);
        return clamp(fio2 / 100.0, 0.21, 1.0);
    }

    // NEW: null-safe overload that falls back to defaultFrac (e.g., last state)
    public static double fio2ToFrac(Integer fio2OrNull, double defaultFrac) {
        if (fio2OrNull == null) return defaultFrac;
        return fio2ToFrac(fio2OrNull.intValue());
    }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
