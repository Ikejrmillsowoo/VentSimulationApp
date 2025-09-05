package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.Abg;

public final class ValidationRules {
    private ValidationRules() {}

    public static boolean isFatal(Abg a) {
        return a.pH() < 6.8 || a.pH() > 7.8 ||
                a.paCO2() < 15 || a.paCO2() > 120 ||
                a.paO2()  < 30 || a.paO2()  > 600 ||
                a.hco3()  < 5  || a.hco3()  > 60  ||
                a.saO2()  < 50 || a.saO2()  > 100;
    }

    public static boolean isAbnormal(Abg a) {
        return a.pH() < 7.25 || a.pH() > 7.55 ||
                a.paCO2() < 30 || a.paCO2() > 60 ||
                a.paO2()  < 60 || a.paO2()  > 300;
    }
}
