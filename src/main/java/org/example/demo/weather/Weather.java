package org.example.demo.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Класс, представляющий погодные условия
 */
public class Weather {
    
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("temperature")
    private int temperature;
    
    // Конструкторы
    public Weather() {
        this.condition = "";
        this.temperature = 0;
    }
    
    public Weather(String condition, int temperature) {
        this.condition = condition;
        this.temperature = temperature;
    }
    
    // Геттеры и сеттеры
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public int getTemperature() {
        return temperature;
    }
    
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
    
    @Override
    public String toString() {
        return condition + ", " + temperature + "°C";
    }
}