package com.ventsim.ventsim.service;

import com.ventsim.ventsim.dto.InitRequest;
import com.ventsim.ventsim.dto.InitResponse;
import com.ventsim.ventsim.dto.SimulateRequest;
import com.ventsim.ventsim.dto.SimulateResponse;
import com.ventsim.ventsim.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SimulationService {
    private final Map<String, PatientState> store = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SimulationService.class);


    public InitResponse init(InitRequest req) {
        Scenario scenario = Scenario.parse(req.scenario);
        Mode mode = Mode.parse(req.mode);

        double weightKg = (req.weight == null) ? 0.0 : Math.max(0.0, req.weight);
        double vtInit = (req.tidalVolume != null) ? req.tidalVolume : 500.0; // default 500 mL if no TV provided
        int rr = (req.respiratoryRate != null) ? Math.max(6, req.respiratoryRate) : 16;
        double peep = (req.peep != null) ? Math.max(0, req.peep) : 5.0;
        double fio2 = Units.fio2ToFrac((req.fio2 != null) ? req.fio2 : 21);
        Double pinsp = null;
        Double psv = null;

        var prof = DiseaseProfiles.forScenario(scenario);
        Abg abg0 = AbgCalculator.initAbgOrDefault(req.abg, req.scenario);

        PatientState state = new PatientState(
                scenario, mode, weightKg, vtInit, rr, peep, fio2, pinsp, psv,
                abg0, prof.compliance(), prof.resistance(), prof.shunt()
        );
        store.put(state.id(), state);

        double[] target = AbgCalculator.vtTargetRange(weightKg);
        String status = FeedbackEngine.status(abg0);
        String feedback = FeedbackEngine.feedback(scenario, abg0);

        return new InitResponse(state.id(), target[0], target[1], abg0, feedback, status);
    }

    public SimulateResponse simulate(SimulateRequest req) {
        // log the incoming payload (trim if too verbose)
        log.info("simulate stateId={} scenario={} mode={} payload={}",
                req.stateId, req.scenario, req.mode, req);
        PatientState s = store.get(req.stateId);
        if (s == null) {
            return new SimulateResponse(
                    new Abg(7.0, 60, 55, 24, 88, -5),
                    "Unknown stateId. Initialize with /api/init first.",
                    "warning"
            );
        }

        Scenario scenario = Scenario.parse(req.scenario);
        Mode mode = Mode.parse(req.mode);

        // Allow scenario/mode to change mid-flight
        var prof = DiseaseProfiles.forScenario(scenario);
        PatientState current = new PatientState(
                scenario, mode,
                (req.weight != null && req.weight > 0) ? req.weight : s.weightKg(),
                s.vtMl(), s.rr(), s.peep(), s.fio2Frac(),
                s.inspPressure(), s.supportPressure(),
                s.abg(), prof.compliance(), prof.resistance(), prof.shunt()
        );

        // ---- SAFE DEFAULTS (do NOT auto-unbox) ----
        Integer rrReq   = req.respiratoryRate;
        Integer peepReq = req.peep;
        Integer fio2Req = req.fio2;           // integer like 21, 40, 100
        Integer vtReq   = req.tidalVolume;    // only used directly in VC
        Integer pinspReq= req.inspiratoryPressure;
        Integer psvReq  = req.supportPressure;

        // New settings
        int rr = (rrReq != null) ? Math.max(6, rrReq) : s.rr();
        double peep = (peepReq != null) ? Math.max(0, peepReq) : s.peep();
        double fio2 = (fio2Req != null) ? Units.fio2ToFrac(fio2Req) : s.fio2Frac();

        Double pinsp = Double.valueOf((pinspReq != null) ? Math.max(0, pinspReq) : null); // delta above PEEP for PC
        Double psv   = Double.valueOf((psvReq   != null) ? Math.max(0, psvReq)   : null); // PS above PEEP for PSV

        // VC uses the requested TV; PC/PSV compute TV from pressure*compliance
        double vtRequested = (vtReq != null) ? Math.max(100.0, vtReq) : s.vtMl();

        double vtMlFromMode = AbgCalculator.computeVtMlFromMode(
                mode, vtRequested, pinsp, psv, peep, current.compliance(), current.weightKg()
        );

        AbgCalculator.AbgResult res = AbgCalculator.nextAbg(current, vtMlFromMode, rr, peep, fio2);

        current.setVent(vtMlFromMode, rr, peep, fio2, pinsp, psv);
        current.setAbg(res.abg);
        store.put(s.id(), current);

        String status = res.fatal ? "critical" : FeedbackEngine.status(res.abg);
        String feedback = res.fatal ? "Patient did not make it." : FeedbackEngine.feedback(scenario, res.abg);

        return new SimulateResponse(res.abg, feedback, status);
    }
//
//    private static final DecimalFormat df = new DecimalFormat("0.00");
//
//    double paCO2 = 40;
//    double paO2 = 95;
//    double pH = 7.40;
//    double hco3 = 24;
//    double saO2 = 97;
//    double be = 0;
//    String feedback = "Normal ventilation pattern.";
//    String status = "normal";
//    int effectiveTidalVolume = 500; // default assumption
//
//    public SimulationResponse simulate(SimulationRequest request) {
//        String mode = request.getMode();
//        String scenario = request.getScenario();
//
//        // Step 1: Determine ventilation status
//        int rr = request.getRespiratoryRate();
//        updateVolumeBasedOnMode(mode, request);
//        // Step 2: Modify values based on ventilation effectiveness
//        updateBasedOnMinuteVolume(rr);
//
//        // Step 3: Modify values based on patient conditions/scenario
//        returnScenario(scenario);
//        ABGResult abg = new ABGResult(df.format(this.pH), paCO2, paO2, hco3, saO2, be);
//        return new SimulationResponse(abg, feedback, status);
//        }
//
//        public void updateVolumeBasedOnMode(String mode, SimulationRequest request){
//            if (mode == null || mode.equalsIgnoreCase("Volume Control")) {
//                effectiveTidalVolume = request.getTidalVolume();
//            } else if (mode.equalsIgnoreCase("Pressure Control")) {
//                int pressure = request.getInspiratoryPressure() != null ? request.getInspiratoryPressure() : 0;
//                effectiveTidalVolume = pressure * 50;
//            } else if (mode.equalsIgnoreCase("Pressure Support")) {
//                int support = request.getSupportPressure() != null ? request.getSupportPressure() : 0;
//                effectiveTidalVolume = support * 40;
//            } else if (mode.equalsIgnoreCase("CPAP")) {
//                effectiveTidalVolume = 0; // Spontaneous breathing only
//            }
//
//        }
//        public void updateBasedOnMinuteVolume(int rr){
//            int minuteVentilation = effectiveTidalVolume * rr;
//            if (minuteVentilation < 4000) {
//                paCO2 += 10;
//                pH -= 0.08;
//                feedback = "Hypoventilation: Consider increasing tidal volume or rate.";
//                status = "warning";
//            } else if (minuteVentilation > 9000) {
//                paCO2 -= 10;
//                pH += 0.08;
//                feedback = "Hyperventilation: Risk of respiratory alkalosis.";
//                status = "warning";
//            }
//        }
//        public void returnScenario(String scenario){
//        // Step 3: Modify values based on patient conditions/scenario
//        if (scenario != null) {
//            switch (scenario.toLowerCase()) {
//                case "ards":
//                    paO2 -= 20;
//                    feedback += " ARDS: Consider increasing PEEP or FiO2.";
//                    break;
//                case "copd":
//                    paCO2 += 10;
//                    hco3 += 6;
//                    pH -= 0.05;
//                    feedback += " COPD: Watch for CO2 retention.";
//                    break;
//                case "asthma":
//                    paCO2 += 5;
//                    feedback += " Asthma: Risk of dynamic hyperinflation.";
//                    break;
//                case "neuromuscular":
//                    paCO2 += 15;
//                    pH -= 0.10;
//                    feedback += " Neuromuscular: Severe hypoventilation.";
//                    status = "critical";
//                    break;
//                case "sedation":
//                    paCO2 += 20;
//                    pH -= 0.12;
//                    feedback += " Sedation: Depressed respiratory drive.";
//                    status = "critical";
//                    break;
//                case "metabolic":
//                    paCO2 -= 10;
//                    pH -= 0.05;
//                    hco3 -= 6;
//                    feedback += " Metabolic acidosis: Compensatory hyperventilation expected.";
//                    break;
//                case "pe":
//                    paO2 -= 25;
//                    feedback += " Suspected PE: Hypoxia with normal or low CO2.";
//                    break;
//                case "obesity":
//                    paCO2 += 8;
//                    hco3 += 4;
//                    pH -= 0.04;
//                    feedback += " Obesity Hypoventilation: Reduced chest wall compliance.";
//                    break;
//                default:
//                    break;
//            }
//        }
//
//
//    }


    }


