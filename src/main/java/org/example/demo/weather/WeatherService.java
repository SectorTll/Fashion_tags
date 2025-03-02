package org.example.demo.weather;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.demo.AppSettings;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Сервис для работы с погодными условиями
 */
public class WeatherService {
    private WeatherConditions weatherConditions;
    private static WeatherService instance;
    
    private WeatherService() {
        loadWeatherConditions();
    }
    
    public static WeatherService getInstance() {
        if (instance == null) {
            instance = new WeatherService();
        }
        return instance;
    }
    
    public void loadWeatherConditions() {
        String weatherFile = AppSettings.getInstance().getWeatherFile();
        if (weatherFile != null && !weatherFile.isEmpty()) {
            try {
                File file = new File(weatherFile);
                if (file.exists()) {
                    weatherConditions = WeatherConditions.loadFromFile(weatherFile);
                } else {
                    // Если файл не существует, создаем новый экземпляр
                    weatherConditions = new WeatherConditions();
                    // Создаем родительскую директорию
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    // Сохраняем пустой файл
                    saveWeatherConditions();
                }
            } catch (IOException e) {
                weatherConditions = new WeatherConditions();
                showError("Ошибка загрузки погодных условий", e.getMessage());
            }
        } else {
            // Если путь не указан, создаем путь по умолчанию
            String userHome = System.getProperty("user.home");
            Path defaultPath = Paths.get(userHome, ".clothesml", "weather.json");
            AppSettings.getInstance().setWeatherFile(defaultPath.toString());
            weatherConditions = new WeatherConditions();
            saveWeatherConditions();
        }
    }
    
    public void saveWeatherConditions() {
        String weatherFile = AppSettings.getInstance().getWeatherFile();
        if (weatherFile != null && !weatherFile.isEmpty()) {
            try {
                weatherConditions.saveToFile(weatherFile);
            } catch (IOException e) {
                showError("Ошибка сохранения погодных условий", e.getMessage());
            }
        }
    }
    
    public void addWeather(Weather weather) {
        // Проверяем, существует ли уже такое погодное условие
        Optional<Weather> existingWeather = weatherConditions.getByConditionAndTemperature(
                weather.getCondition(), weather.getTemperature());
        
        if (existingWeather.isPresent()) {
            // Если существует, удаляем старое
            weatherConditions.removeWeather(existingWeather.get());
        }
        
        // Добавляем новое
        weatherConditions.addWeather(weather);
        saveWeatherConditions();
    }
    
    public void removeWeather(Weather weather) {
        weatherConditions.removeWeather(weather);
        saveWeatherConditions();
    }
    
    public List<Weather> getAllWeatherConditions() {
        return weatherConditions.getWeatherConditions();
    }
    
    /**
     * Получить список всех доступных погодных условий
     */
    public List<String> getAvailableConditions() {
        return new ArrayList<>(WeatherConditions.AVAILABLE_CONDITIONS);
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}