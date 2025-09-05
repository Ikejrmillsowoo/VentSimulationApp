package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.Abg;
import com.ventsim.ventsim.model.Scenario;

public final class FeedbackEngine {
    private FeedbackEngine(){}

    public static String feedback(Scenario sc, Abg a) {
        if (ValidationRules.isFatal(a)) return "Patient did not make it.";

        StringBuilder sb = new StringBuilder();
        if (a.paCO2() > 55) sb.append("Hypoventilation: consider ↑RR or ↑Vt (watch plateau/auto-PEEP). ");
        if (a.paCO2() < 30) sb.append("Hyperventilation: risk of alkalosis; consider ↓RR or ↓Vt. ");
        if (a.paO2() < 60)  sb.append("Hypoxemia: consider ↑FiO2 or ↑PEEP. ");
        if (a.pH() < 7.25)  sb.append("Acidemia: correct ventilation; evaluate metabolic status. ");
        if (a.pH() > 7.55)  sb.append("Alkalemia: reduce minute ventilation if appropriate. ");

        if (sc == Scenario.ARDS) sb.append("ARDS: favor low Vt (4–6 mL/kg), higher PEEP strategy. ");
        if (sc == Scenario.COPD) sb.append("COPD: avoid air trapping; allow longer TE and moderate PEEP. ");
        if (sc == Scenario.ASTHMA) sb.append("Asthma: long expiration, permissive hypercapnia may be acceptable. ");

        if (sb.length() == 0) sb.append("Parameters within acceptable range. Continue monitoring.");
        return sb.toString().trim();
    }

    public static String status(Abg a) {
        if (ValidationRules.isFatal(a)) return "critical";
        if (ValidationRules.isAbnormal(a)) return "warning";
        return "ok";
    }
}
