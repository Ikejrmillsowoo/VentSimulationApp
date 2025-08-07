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
        } else {
            abg = new ABGResult(7.40, 40, 95, 24, 97, 0);
            feedback = "Scenario not recognized. Default response given.";
            status = "normal";
        }

        return new SimulationResponse(abg, feedback, status);
    }
}
