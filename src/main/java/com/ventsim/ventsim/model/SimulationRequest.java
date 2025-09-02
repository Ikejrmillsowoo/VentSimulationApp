package com.ventsim.ventsim.model;

public class SimulationRequest {
    private String scenario;
    private int tidalVolume;
    private int respiratoryRate;
    private int peep;
    private int fio2;
    private String mode;
    private int weight;
    private int targetVolume;
    private Integer inspiratoryPressure;
    private Integer supportPressure;

    public SimulationRequest(String scenario, int tidalVolume, int respiratoryRate, int peep, int fio2, String mode, int weight, Integer inspiratoryPressure, Integer supportPressure) {
        this.scenario = scenario;
        this.tidalVolume = tidalVolume;
        this.respiratoryRate = respiratoryRate;
        this.peep = peep;
        this.fio2 = fio2;
        this.mode = mode;
        this.weight = weight;
        this.inspiratoryPressure = inspiratoryPressure;
        this.supportPressure = supportPressure;
        this.targetVolume = weight * 7;
    }

    public SimulationRequest() {
    }



    // Getters and Setters
    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }
    public int getTidalVolume() { return tidalVolume; }
    public void setTidalVolume(int tidalVolume) { this.tidalVolume = tidalVolume; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(int respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    public int getPeep() { return peep; }
    public void setPeep(int peep) { this.peep = peep; }
    public int getFio2() { return fio2; }
    public void setFio2(int fio2) { this.fio2 = fio2; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public int getWeight() {return weight;}
    public void setWeight(int weight) { this.weight = weight; }
    public Integer getInspiratoryPressure() { return inspiratoryPressure; }
    public void setInspiratoryPressure(Integer inspiratoryPressure) { this.inspiratoryPressure = inspiratoryPressure; }
    public Integer getSupportPressure() { return supportPressure; }
    public void setSupportPressure(Integer supportPressure) { this.supportPressure = supportPressure; }

    public int getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(int targetVolume) {
        this.targetVolume = targetVolume;
    }
}
