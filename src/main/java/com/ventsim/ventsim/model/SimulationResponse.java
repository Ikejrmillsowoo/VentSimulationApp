package com.ventsim.ventsim.model;

public class SimulationResponse {
    private ABGResult abg;
    private String feedback;
    private String status;

    public SimulationResponse(ABGResult abg, String feedback, String status) {
        this.abg = abg;
        this.feedback = feedback;
        this.status = status;
    }

    public ABGResult getAbg() { return abg; }
    public void setAbg(ABGResult abg) { this.abg = abg; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
