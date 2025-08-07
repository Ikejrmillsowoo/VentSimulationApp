package com.ventsim.ventsim.model;

public class SimulationRequest {
    private String scenario;
    private int tidalVolume;
    private int respiratoryRate;
    private int peep;
    private int fio2;
    private String mode;
    private int weight;

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
}
