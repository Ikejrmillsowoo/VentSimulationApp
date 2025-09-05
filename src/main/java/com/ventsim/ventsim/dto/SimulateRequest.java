package com.ventsim.ventsim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SimulateRequest {
    @NotBlank
    public String stateId;
    @NotBlank public String scenario;        // can switch if desired
    @NotBlank public String mode;
    @NotNull
    public Integer tidalVolume;    // mL
      public Integer respiratoryRate;
      public Integer peep;
      public Integer fio2;           // %
    public Double weight;                    // kg (optional override)
    public Integer inspiratoryPressure;      // cmH2O (PC)
    public Integer supportPressure;          // cmH2O (PSV)
}
