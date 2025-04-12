package org.example.demo.weather;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.util.List;

/**
 * Контроллер для экрана управления погодными условиями
 */
public class WeatherViewController {
    
    @FXML
    private ListView<Weather> weatherListView;
    
    @FXML
    private ComboBox<String> conditionComboBox;
    
    @FXML
    private Spinner<Integer> temperatureSpinner;
    
    @FXML
    private Button addButton;
    
    @FXML
    private Button removeButton;
    
    private WeatherService weatherService;
    
    @FXML
    public void initialize() {
        weatherService = WeatherService.getInstance();
        
        // Настраиваем список погодных условий
        refreshWeatherListView();
        
        // Настраиваем отображение ячеек в списке
        weatherListView.setCellFactory(lv -> new ListCell<Weather>() {
            @Override
            protected void updateItem(Weather item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        
        // Заполняем комбобокс доступными погодными условиями
        List<String> availableConditions = weatherService.getAvailableConditions();
        conditionComboBox.setItems(FXCollections.observableArrayList(availableConditions));
        if (!availableConditions.isEmpty()) {
            conditionComboBox.getSelectionModel().selectFirst();
        }
        
        // Настраиваем спиннер для температуры (от -50 до +50 градусов)
        SpinnerValueFactory<Integer> valueFactory = 
                new SpinnerValueFactory.IntegerSpinnerValueFactory(-50, 50, 20);
        temperatureSpinner.setValueFactory(valueFactory);
        
        // Настраиваем кнопку удаления
        removeButton.setDisable(true);
        weatherListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> removeButton.setDisable(newSelection == null)
        );
    }
    
    private void refreshWeatherListView() {
        List<Weather> allWeatherConditions = weatherService.getAllWeatherConditions();
        ObservableList<Weather> observableWeather = FXCollections.observableArrayList(allWeatherConditions);
        weatherListView.setItems(observableWeather);
    }
    
    @FXML
    private void onAddClick() {
        String condition = conditionComboBox.getValue();
        
        if (condition == null || condition.isEmpty()) {
            showAlert("Error", "A weather condition must be selected");
            return;
        }
        
        int temperature = temperatureSpinner.getValue();
        
        // Создаем новое погодное условие
        Weather weather = new Weather(condition, temperature);
        
        // Сохраняем
        weatherService.addWeather(weather);
        
        // Обновляем список
        refreshWeatherListView();

        showInfo("Addition", "Weather condition successfully added");
    }
    
    @FXML
    private void onRemoveClick() {
        Weather selectedWeather = weatherListView.getSelectionModel().getSelectedItem();
        if (selectedWeather != null) {
            weatherService.removeWeather(selectedWeather);
            refreshWeatherListView();
            showInfo("Deletion", "Weather condition successfully deleted");

        }
    }
    
    @FXML
    private void onClearClick() {
        // Сбрасываем значения полей
        if (!conditionComboBox.getItems().isEmpty()) {
            conditionComboBox.getSelectionModel().selectFirst();
        }
        temperatureSpinner.getValueFactory().setValue(20);
    }
    
    @FXML
    private void onCloseClick() {
        ((Stage) weatherListView.getScene().getWindow()).close();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}