package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.ABGResult;
import com.ventsim.ventsim.model.SimulationRequest;
import com.ventsim.ventsim.model.SimulationResponse;
import org.springframework.stereotype.Service;

@Service
public class SimulationService {
    public SimulationResponse simulate(SimulationRequest request) {
        String scenario = request.getScenario();
        int tv = request.getTidalVolume();
        int rr = request.getRespiratoryRate();
        int peep = request.getPeep();
        int fio2 = request.getFio2();
        int weight = request.getWeight();
        // eventually I will add I-time, patient weight and calculation for normal/ predicted VT.
        /*
        * predictedTvLow = > (weight (in kgs) * 6)
        * predictedTvLow = < (weight (in kgs) * 8)
        * */
        int lowWeightLimit = weight * 6;
        int highWeightLimit = weight * 8;

        ABGResult abg;
        String feedback;
        String status;

        if (scenario.equalsIgnoreCase("Normal")) {
            if (tv < lowWeightLimit && rr < 10) {
                abg = new ABGResult(7.28, 55, 88, 24, 95, 0);
                feedback = "Hypoventilation: Increase tidal volume or rate.";
                status = "warning";
            } else if (tv > highWeightLimit) {
                abg = new ABGResult(7.48, 28, 102, 24, 100, 2);
                feedback = "Risk of respiratory alkalosis and barotrauma.";
                status = "critical";
            } else {
                abg = new ABGResult(7.40, 40, 95, 24, 97, 0);
                feedback = "Ventilation within normal range.";
                status = "normal";
            }
        } else if (scenario.equalsIgnoreCase("ARDS")) {
            if (peep < 10) {
                abg = new ABGResult(7.36, 44, 70, 24, 92, 0);
                feedback = "Low oxygenation; increase PEEP or FiO2.";
                status = "warning";
            } else {
                abg = new ABGResult(7.38, 45, 88, 24, 96, 0);
                feedback = "Improved oxygenation with PEEP.";
                status = "normal";
            }
        } else if (scenario.equalsIgnoreCase("COPD")) {
            if (rr > 14) {
                abg = new ABGResult(7.50, 30, 100, 30, 100, 3);
                feedback = "Overventilating a CO2 retainer; risk of alkalosis.";
                status = "critical";
            } else {
                abg = new ABGResult(7.35, 55, 68, 30, 94, 2);
                feedback = "Acceptable for chronic retention. Avoid aggressive ventilation.";
                status = "normal";
            }
        } else if (scenario.equalsIgnoreCase("Asthma")) {
            if (rr > 20) {
                abg = new ABGResult(7.52, 28, 95, 24, 98, 2);
                feedback = "Patient may be hyperventilating during bronchospasm.";
                status = "warning";
            } else {
                abg = new ABGResult(7.38, 40, 92, 24, 96, 0);
                feedback = "Stable ventilation. Monitor airway resistance.";
                status = "normal";
            }
        } else if (scenario.equalsIgnoreCase("Neuromuscular")) {
            if (tv < 300) {
                abg = new ABGResult(7.25, 60, 80, 24, 90, -2);
                feedback = "Severe hypoventilation. Consider ventilatory support upgrade.";
                status = "critical";
            } else {
                abg = new ABGResult(7.36, 45, 90, 24, 95, 0);
                feedback = "Acceptable support. Monitor for fatigue.";
                status = "normal";
            }
        } else if (scenario.equalsIgnoreCase("Sedation")) {
            if (tv < 300 || rr < 8) {
                abg = new ABGResult(7.22, 65, 78, 24, 90, -4);
                feedback = "Sedation-induced respiratory depression. Urgent intervention needed.";
                status = "critical";
            } else {
                abg = new ABGResult(7.34, 50, 85, 24, 93, -1);
                feedback = "Mild hypoventilation. Monitor closely.";
                status = "warning";
            }
        } else if (scenario.equalsIgnoreCase("Metabolic")) {
            if (rr > 18) {
                abg = new ABGResult(7.32, 30, 95, 18, 96, -4);
                feedback = "Compensatory hyperventilation for metabolic acidosis.";
                status = "warning";
            } else {
                abg = new ABGResult(7.28, 38, 92, 18, 94, -5);
                feedback = "Inadequate compensation for acidosis. Increase ventilatory rate.";
                status = "critical";
            }
        } else if (scenario.equalsIgnoreCase("PE")) {
            abg = new ABGResult(7.40, 40, 72, 24, 90, 0);
            feedback = "Suspected pulmonary embolism: Low oxygen despite normal CO2. Consider imaging.";
            status = "warning";
        } else if (scenario.equalsIgnoreCase("Obesity")) {
            if (tv < 400) {
                abg = new ABGResult(7.36, 50, 85, 28, 92, 1);
                feedback = "Hypoventilation common in obesity. Consider higher tidal volume.";
                status = "warning";
            } else {
                abg = new ABGResult(7.38, 45, 90, 26, 94, 0);
                feedback = "Compensated ventilation in obesity hypoventilation syndrome.";
                status = "normal";
            }
        } else {
            abg = new ABGResult(7.40, 40, 95, 24, 97, 0);
            feedback = "Scenario not recognized. Default response given.";
            status = "normal";
        }


        return new SimulationResponse(abg, feedback, status);
    }
}
