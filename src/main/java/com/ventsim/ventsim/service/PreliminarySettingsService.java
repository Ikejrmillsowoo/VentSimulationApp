package com.ventsim.ventsim.service;

import com.ventsim.ventsim.model.SimulationRequest;
import com.ventsim.ventsim.model.SimulationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class PreliminarySettingsService {

//    @Autowired
//    private SimulationRequest simulationRequest;

    public int getDefaultSetting(@RequestBody int weight){
        SimulationRequest sim = new SimulationRequest();
//        sim.setWeight(weight);
        int targetVolume = sim.getTargetVolume();
        return targetVolume;
    }
}
