package com.ventsim.ventsim.model;

public class ABGResult {
    private String pH;
    private double paCO2;
    private double paO2;
    private double hco3;
    private double saO2;
    private double be;

    public ABGResult() {}

    public ABGResult(String pH, double paCO2, double paO2, double hco3, double saO2, double be) {
        this.pH = pH;
        this.paCO2 = paCO2;
        this.paO2 = paO2;
        this.hco3 = hco3;
        this.saO2 = saO2;
        this.be = be;
    }

    public String getpH() { return pH; }
    public void setpH(String pH) { this.pH = pH; }
    public double getPaCO2() { return paCO2; }
    public void setPaCO2(double paCO2) { this.paCO2 = paCO2; }
    public double getPaO2() { return paO2; }
    public void setPaO2(double paO2) { this.paO2 = paO2; }
    public double getHco3() { return hco3; }
    public void setHco3(double hco3) { this.hco3 = hco3; }
    public double getSaO2() { return saO2; }
    public void setSaO2(double saO2) { this.saO2 = saO2; }
    public double getBe() { return be; }
    public void setBe(double be) { this.be = be; }
}
