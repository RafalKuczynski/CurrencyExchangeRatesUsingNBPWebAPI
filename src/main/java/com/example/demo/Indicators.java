package com.example.demo;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Indicators {

    private LocalDate minDate;
    private LocalDate maxDate;
    private Map<String, Double> mapMin = new HashMap<>();
    private Map<String, Double> mapMax = new HashMap<>();

    public Indicators() {
    }

    public LocalDate getMinDate() {
        return minDate;
    }

    public void setMinDate(LocalDate minDate) {
        this.minDate = minDate;
    }

    public LocalDate getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(LocalDate maxDate) {
        this.maxDate = maxDate;
    }

    public Map<String, Double> getMapMin() {
        return mapMin;
    }

    public void setMapMin(Map<String, Double> mapMin) {
        this.mapMin = mapMin;
    }

    public Map<String, Double> getMapMax() {
        return mapMax;
    }

    public void setMapMax(Map<String, Double> mapMax) {
        this.mapMax = mapMax;
    }

}
