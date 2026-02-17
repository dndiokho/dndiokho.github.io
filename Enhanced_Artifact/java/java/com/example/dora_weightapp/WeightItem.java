package com.example.dora_weightapp;

public class WeightItem {
    public int id;
    public String date;
    public double weight;

    public WeightItem(int id, String date, double weight) {
        this.id = id;
        this.date = date;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return date + " - " + weight + " kg";
    }
}
