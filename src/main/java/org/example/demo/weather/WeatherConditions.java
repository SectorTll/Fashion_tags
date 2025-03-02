package org.example.demo.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс для хранения коллекции погодных условий
 */
public class WeatherConditions {
    
    @JsonProperty("weather_conditions")
    private List<Weather> weatherConditions;
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // Доступные погодные условия
    public static final List<String> AVAILABLE_CONDITIONS = List.of(
        "sunny", "cloudy", "rainy", "snowy", "windy", "foggy",
        "drizzle", "heavy rain", "storm", "thunderstorm", "hail",
        "icy", "hot", "cold", "humid", "dry"
    );
    
    // Конструкторы
    public WeatherConditions() {
        this.weatherConditions = new ArrayList<>();
    }
    
    // Методы для работы с коллекцией погодных условий
    public void addWeather(Weather weather) {
        weatherConditions.add(weather);
    }
    
    public void removeWeather(Weather weather) {
        weatherConditions.remove(weather);
    }
    
    public List<Weather> getWeatherConditions() {
        return weatherConditions;
    }
    
    public void setWeatherConditions(List<Weather> weatherConditions) {
        this.weatherConditions = weatherConditions;
    }
    
    // Получение погоды по условию и температуре
    public Optional<Weather> getByConditionAndTemperature(String condition, int temperature) {
        return weatherConditions.stream()
                .filter(w -> w.getCondition().equals(condition) && w.getTemperature() == temperature)
                .findFirst();
    }
    
    // Сохранение и загрузка
    public void saveToFile(String filepath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(filepath), this);
    }
    
    public static WeatherConditions loadFromFile(String filepath) throws IOException {
        return mapper.readValue(new File(filepath), WeatherConditions.class);
    }
}