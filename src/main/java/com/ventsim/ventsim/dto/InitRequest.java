package com.ventsim.ventsim.dto;

import com.ventsim.ventsim.model.Abg;
import jakarta.validation.constraints.NotBlank;

public class InitRequest {
    public Double weight;          // kg (optional)
    @NotBlank public String scenario;
    @NotBlank public String mode;
    public Integer tidalVolume;    // mL
    public Integer respiratoryRate;
    public Integer peep;           // cmH2O
    public Integer fio2;           // % (e.g., 21, 40, 100)
    public String note;
    public Abg abg;                // optional seed
}
