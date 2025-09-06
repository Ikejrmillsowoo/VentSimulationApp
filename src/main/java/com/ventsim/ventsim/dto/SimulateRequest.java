package com.ventsim.ventsim.dto;



public class SimulateRequest {

    public String stateId;
     public String scenario;        // can switch if desired
     public String mode;

    public Integer tidalVolume;    // mL
      public Integer respiratoryRate;
      public Integer peep;
      public Integer fio2;           // %
    public Double weight;                    // kg (optional override)
    public Integer inspiratoryPressure;      // cmH2O (PC)
    public Integer supportPressure;// cmH2O (PSV)

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getScenario() {
        return scenario;
    }

    public void setScenario(String scenario) {
        this.scenario = scenario;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getTidalVolume() {
        return tidalVolume;
    }

    public void setTidalVolume(Integer tidalVolume) {
        this.tidalVolume = tidalVolume;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getPeep() {
        return peep;
    }

    public void setPeep(Integer peep) {
        this.peep = peep;
    }

    public Integer getFio2() {
        return fio2;
    }

    public void setFio2(Integer fio2) {
        this.fio2 = fio2;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getInspiratoryPressure() {
        return inspiratoryPressure;
    }

    public void setInspiratoryPressure(Integer inspiratoryPressure) {
        this.inspiratoryPressure = inspiratoryPressure;
    }

    public Integer getSupportPressure() {
        return supportPressure;
    }

    public void setSupportPressure(Integer supportPressure) {
        this.supportPressure = supportPressure;
    }
}
