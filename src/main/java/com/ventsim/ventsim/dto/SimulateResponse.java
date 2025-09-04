package com.ventsim.ventsim.dto;

import com.ventsim.ventsim.model.Abg;

public record SimulateResponse(
    Abg abg,
    String feedback,
    String status
){}
