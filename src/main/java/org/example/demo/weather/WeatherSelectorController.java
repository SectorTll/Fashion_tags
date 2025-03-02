package org.example.demo.weather;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для окна выбора погодных условий
 */
public class WeatherSelectorController {
    
    @FXML
    private ComboBox<Weather> weatherComboBox;
    
    @FXML
    private Label selectedWeatherLabel;
    
    private WeatherService weatherService;
    private Weather selectedWeather;
    private boolean selectionConfirmed = false;
    
    @FXML
    public void initialize() {
        weatherService = WeatherService.getInstance();
        
        // Настраиваем отображение элементов в комбобоксе
        weatherComboBox.setConverter(new StringConverter<Weather>() {
            @Override
            public String toString(Weather weather) {
                return weather != null ? weather.toString() : "";
            }
            
            @Override
            public Weather fromString(String string) {
                // Этот метод не используется для ComboBox
                return null;
            }
        });
        
        // Загружаем погодные условия в комбобокс
        refreshWeatherComboBox();
        
        // Добавляем обработчик выбора погодного условия
        weatherComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                selectedWeatherLabel.setText(newValue.toString());
                selectedWeather = newValue;
            } else {
                selectedWeatherLabel.setText("");
                selectedWeather = null;
            }
        });
    }
    
    /**
     * Обновляет комбобокс с погодными условиями
     */
    private void refreshWeatherComboBox() {
        List<Weather> weatherConditions = weatherService.getAllWeatherConditions();
        
        weatherComboBox.setItems(FXCollections.observableArrayList(weatherConditions));
        
        // Если есть погодные условия, выбираем первое
        if (!weatherConditions.isEmpty()) {
            weatherComboBox.getSelectionModel().selectFirst();
        }
    }
    
    /**
     * Открывает окно управления погодными условиями
     */
    @FXML
    private void onManageWeatherClick() {
        try {
            // Запоминаем текущий выбор
            Weather currentSelection = weatherComboBox.getValue();
            
            // Загружаем окно управления погодой
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/weather-view.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Управление погодными условиями");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(weatherComboBox.getScene().getWindow());
            
            stage.showAndWait();
            
            // Обновляем комбобокс
            refreshWeatherComboBox();
            
            // Пытаемся восстановить выбор
            if (currentSelection != null) {
                for (Weather weather : weatherComboBox.getItems()) {
                    if (weather.getCondition().equals(currentSelection.getCondition()) && 
                        weather.getTemperature() == currentSelection.getTemperature()) {
                        weatherComboBox.setValue(weather);
                        break;
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Подтверждает выбор погодного условия
     */
    @FXML
    private void onSelectClick() {
        if (selectedWeather != null) {
            selectionConfirmed = true;
            ((Stage) weatherComboBox.getScene().getWindow()).close();
        }
    }
    
    /**
     * Отменяет выбор погодного условия
     */
    @FXML
    private void onCancelClick() {
        selectedWeather = null;
        selectionConfirmed = false;
        ((Stage) weatherComboBox.getScene().getWindow()).close();
    }
    
    /**
     * Возвращает выбранное погодное условие
     */
    public Weather getSelectedWeather() {
        return selectionConfirmed ? selectedWeather : null;
    }
    
    /**
     * Статический метод для открытия диалога выбора погодного условия
     */
    public static Optional<Weather> showAndWait(Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(WeatherSelectorController.class.getResource("/org/example/demo/weather-selector-view.fxml"));
            Parent root = loader.load();
            
            WeatherSelectorController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.setTitle("Выбор погодных условий");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);
            
            stage.showAndWait();
            
            return Optional.ofNullable(controller.getSelectedWeather());
            
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}