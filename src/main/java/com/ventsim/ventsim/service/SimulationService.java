package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.ABGResult;
import com.ventsim.ventsim.model.SimulationRequest;
import com.ventsim.ventsim.model.SimulationResponse;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {
    public SimulationResponse simulate(SimulationRequest request) {
        String mode = request.getMode();
        String scenario = request.getScenario();

        // Step 1: Determine ventilation status
        int effectiveTidalVolume = 500; // default assumption
        int rr = request.getRespiratoryRate();

        if (mode == null || mode.equalsIgnoreCase("Volume Control")) {
            effectiveTidalVolume = request.getTidalVolume();
        } else if (mode.equalsIgnoreCase("Pressure Control")) {
            int pressure = request.getInspiratoryPressure() != null ? request.getInspiratoryPressure() : 0;
            effectiveTidalVolume = pressure * 50;
        } else if (mode.equalsIgnoreCase("Pressure Support")) {
            int support = request.getSupportPressure() != null ? request.getSupportPressure() : 0;
            effectiveTidalVolume = support * 40;
        } else if (mode.equalsIgnoreCase("CPAP")) {
            effectiveTidalVolume = 0; // Spontaneous breathing only
        }

        double paCO2 = 40;
        double paO2 = 95;
        double pH = 7.40;
        double hco3 = 24;
        double saO2 = 97;
        double be = 0;
        String feedback = "Normal ventilation pattern.";
        String status = "normal";

        // Step 2: Modify values based on ventilation effectiveness
        int minuteVentilation = effectiveTidalVolume * rr;
        if (minuteVentilation < 4000) {
            paCO2 += 10;
            pH -= 0.08;
            feedback = "Hypoventilation: Consider increasing tidal volume or rate.";
            status = "warning";
        } else if (minuteVentilation > 9000) {
            paCO2 -= 10;
            pH += 0.08;
            feedback = "Hyperventilation: Risk of respiratory alkalosis.";
            status = "warning";
        }

        // Step 3: Modify values based on scenario
        if (scenario != null) {
            switch (scenario.toLowerCase()) {
                case "ards":
                    paO2 -= 20;
                    feedback += " ARDS: Consider increasing PEEP or FiO2.";
                    break;
                case "copd":
                    paCO2 += 10;
                    hco3 += 6;
                    pH -= 0.05;
                    feedback += " COPD: Watch for CO2 retention.";
                    break;
                case "asthma":
                    paCO2 += 5;
                    feedback += " Asthma: Risk of dynamic hyperinflation.";
                    break;
                case "neuromuscular":
                    paCO2 += 15;
                    pH -= 0.10;
                    feedback += " Neuromuscular: Severe hypoventilation.";
                    status = "critical";
                    break;
                case "sedation":
                    paCO2 += 20;
                    pH -= 0.12;
                    feedback += " Sedation: Depressed respiratory drive.";
                    status = "critical";
                    break;
                case "metabolic":
                    paCO2 -= 10;
                    pH -= 0.05;
                    hco3 -= 6;
                    feedback += " Metabolic acidosis: Compensatory hyperventilation expected.";
                    break;
                case "pe":
                    paO2 -= 25;
                    feedback += " Suspected PE: Hypoxia with normal or low CO2.";
                    break;
                case "obesity":
                    paCO2 += 8;
                    hco3 += 4;
                    pH -= 0.04;
                    feedback += " Obesity Hypoventilation: Reduced chest wall compliance.";
                    break;
                default:
                    break;
            }
        }

        ABGResult abg = new ABGResult(pH, paCO2, paO2, hco3, saO2, be);
        return new SimulationResponse(abg, feedback, status);
    }
}
