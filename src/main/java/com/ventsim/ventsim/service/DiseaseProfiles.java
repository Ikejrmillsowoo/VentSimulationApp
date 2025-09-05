package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.Scenario;

public class DiseaseProfiles {
    public record Profile(double compliance, double resistance, double shunt) {}

    public static Profile forScenario(Scenario s) {
        return switch (s) {
            case NORMAL -> new Profile(50, 10, 0.05);
            case COPD   -> new Profile(60, 25, 0.12);
            case ARDS   -> new Profile(20, 12, 0.30);
            case ASTHMA -> new Profile(45, 35, 0.10);
        };
    }
}
