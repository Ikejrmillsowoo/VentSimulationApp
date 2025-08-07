package com.ventsim.ventsim.controller;

import com.ventsim.ventsim.model.SimulationRequest;
import com.ventsim.ventsim.model.SimulationResponse;
import com.ventsim.ventsim.service.SimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/simulate")
public class SimulationController {
    @Autowired
    private SimulationService simulationService;

    @PostMapping
    public ResponseEntity<SimulationResponse> runSimulation(@RequestBody SimulationRequest request) {
        SimulationResponse response = simulationService.simulate(request);
        return ResponseEntity.ok(response);
    }
}
