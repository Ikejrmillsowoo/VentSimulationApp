package com.ventsim.ventsim.model;

public class ConditionalRequest {
    private String condition;
    private int weight;


    public ConditionalRequest(String condition, int weight) {
        this.condition = condition;
        this.weight = weight;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
