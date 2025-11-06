package com.ventsim.ventsim.controller;

import com.ventsim.ventsim.dto.InitRequest;
import com.ventsim.ventsim.dto.InitResponse;
import com.ventsim.ventsim.dto.SimulateRequest;
import com.ventsim.ventsim.dto.SimulateResponse;

import com.ventsim.ventsim.service.SimulationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/simulate")
public class SimulationController {
    private final SimulationService service;
    public SimulationController(SimulationService service) { this.service = service; }

    @PostMapping(value="/init", consumes= MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public InitResponse init(@Valid @RequestBody InitRequest request) {
        return service.init(request);
    }

    @PostMapping(value="/simulate", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    public SimulateResponse simulate(@Valid @RequestBody SimulateRequest request) {
        return service.simulate(request);
    }

    @GetMapping("/health")
    public String health() { return "OK"; }


}
