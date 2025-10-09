package com.ventsim.ventsim.service;

import com.ventsim.ventsim.dto.InitRequest;
import com.ventsim.ventsim.dto.InitResponse;
import com.ventsim.ventsim.dto.SimulateRequest;
import com.ventsim.ventsim.dto.SimulateResponse;
import com.ventsim.ventsim.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


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
        Double pinsp = (req.insPressure != null) ? Math.max(0.0, req.insPressure) : null;
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

        // inside simulate(..) AFTER you load the current state and build the current`
        Integer rrReq        = req.getRespiratoryRate();      // could be null
        Integer peepReq      = req.getPeep();                 // could be null
        Integer fio2Req      = req.getFio2();                 // could be null (e.g., 21, 40, 100)
        Integer vtReq        = req.getTidalVolume();          // could be null (VC only)
        Integer pinspReq     = req.getInspiratoryPressure();  // could be null (PC)
        Integer psvReq       = req.getSupportPressure();      // could be null (PSV)

        // DEFAULTS that never auto-unbox
        int rr              = (rrReq   != null) ? Math.max(6, rrReq) : s.rr();
        double peep         = (peepReq != null) ? Math.max(0, peepReq) : s.peep();
        double fio2Frac     = Units.fio2ToFrac(fio2Req, s.fio2Frac()); // null-safe overload

        Double pinsp        = (pinspReq != null) ? Double.valueOf(Math.max(0, pinspReq)) : null;
        Double psv          = (psvReq   != null) ? Double.valueOf(Math.max(0, psvReq))   : null;



        // VC uses the requested TV; PC/PSV compute TV from pressure*compliance
        double vtRequested = (vtReq != null) ? Math.max(100.0, vtReq) : s.vtMl();

        double vtMlFromMode = AbgCalculator.computeVtMlFromMode(
                mode, vtRequested, pinsp, psv, peep, current.compliance(), current.weightKg()
        );

        // Compute next ABG
        AbgCalculator.AbgResult res = AbgCalculator.nextAbg(current, vtMlFromMode, rr, peep, fio2Frac);

        // Update and return
        current.setVent(vtMlFromMode, rr, peep, fio2Frac, pinsp, psv);
        current.setAbg(res.abg);
        store.put(s.id(), current);

        String status   = res.fatal ? "critical" : FeedbackEngine.status(res.abg);
        String feedback = res.fatal ? "Patient did not make it." : FeedbackEngine.feedback(scenario, res.abg);
        return new SimulateResponse(res.abg, feedback, status);
    }

    }


