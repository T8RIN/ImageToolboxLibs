package com.jhlabs;

public class TemperatureFilter extends PointFilter {
    private float temperature = 6650.0f;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public String toString() {
        return "Colors/Temperature...";
    }
}
