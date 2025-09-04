package com.ventsim.ventsim.dto;

import com.ventsim.ventsim.model.Abg;

public record InitResponse (
        String stateId,
        double targetVtMinMl,
        double targetVtMaxMl,
        Abg abg,
        String feedback,
        String status
) {}
