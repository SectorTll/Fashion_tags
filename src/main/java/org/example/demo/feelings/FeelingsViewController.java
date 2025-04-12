package org.example.demo.feelings;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.FlowPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для экрана управления наборами стилей
 */
public class FeelingsViewController {
    
    @FXML
    private ListView<Feeling> feelingsListView;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private FlowPane stylesFlowPane;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button removeButton;
    
    @FXML
    private Button editButton;
    
    @FXML
    private Label createEditLabel;
    
    private FeelingsService feelingsService;
    private Map<String, CheckBox> styleCheckboxes;
    private Feeling currentlyEditing;
    
    @FXML
    public void initialize() {
        feelingsService = FeelingsService.getInstance();
        styleCheckboxes = new HashMap<>();
        
        // Настраиваем список наборов стилей
        refreshFeelingsListView();
        
        // Настраиваем отображение ячеек в списке
        feelingsListView.setCellFactory(lv -> new ListCell<Feeling>() {
            @Override
            protected void updateItem(Feeling item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        
        // Наполняем панель чекбоксами для стилей
        setupStyleCheckboxes();
        
        // Настраиваем кнопки
        removeButton.setDisable(true);
        editButton.setDisable(true);
        
        // Обработчик выбора набора в списке
        feelingsListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                boolean hasSelection = newSelection != null;
                removeButton.setDisable(!hasSelection);
                editButton.setDisable(!hasSelection);
            }
        );
        
        // Сбрасываем форму редактирования
        resetEditForm();
    }
    
    private void setupStyleCheckboxes() {
        List<String> availableStyles = feelingsService.getAvailableStyles();
        stylesFlowPane.getChildren().clear();
        styleCheckboxes.clear();
        
        for (String style : availableStyles) {
            CheckBox checkBox = new CheckBox(style);
            styleCheckboxes.put(style, checkBox);
            stylesFlowPane.getChildren().add(checkBox);
        }
    }
    
    private void refreshFeelingsListView() {
        List<Feeling> allFeelings = feelingsService.getAllFeelings();
        ObservableList<Feeling> observableFeelings = FXCollections.observableArrayList(allFeelings);
        feelingsListView.setItems(observableFeelings);
    }
    
    @FXML
    private void onSaveClick() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            showAlert("Error", "A style set name must be specified");

            return;
        }
        
        // Создаем список выбранных стилей
        List<String> selectedStyles = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : styleCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedStyles.add(entry.getKey());
            }
        }
        
        if (selectedStyles.isEmpty()) {
            showAlert("Error", "Select at least one style");

            return;
        }
        
        // Создаем или обновляем набор стилей
        Feeling feeling;
        if (currentlyEditing != null) {
            feeling = currentlyEditing;
            feeling.setName(name);
            feeling.setStyles(selectedStyles);
        } else {
            feeling = new Feeling(name, selectedStyles);
        }
        
        // Сохраняем набор
        feelingsService.addFeeling(feeling);
        
        // Обновляем список и сбрасываем форму
        refreshFeelingsListView();
        resetEditForm();
        
        showInfo("Save", "Style set saved successfully");
    }
    
    @FXML
    private void onRemoveClick() {
        Feeling selectedFeeling = feelingsListView.getSelectionModel().getSelectedItem();
        if (selectedFeeling != null) {
            feelingsService.removeFeeling(selectedFeeling.getName());
            refreshFeelingsListView();
            resetEditForm();
            showInfo("Deletion", "Style set successfully deleted");

        }
    }
    
    @FXML
    private void onEditClick() {
        Feeling selectedFeeling = feelingsListView.getSelectionModel().getSelectedItem();
        if (selectedFeeling != null) {
            // Устанавливаем текущий набор для редактирования
            currentlyEditing = selectedFeeling;
            
            // Заполняем форму данными из выбранного набора
            nameField.setText(selectedFeeling.getName());
            
            // Сбрасываем все чекбоксы
            styleCheckboxes.values().forEach(cb -> cb.setSelected(false));
            
            // Отмечаем выбранные стили
            for (String style : selectedFeeling.getStyles()) {
                CheckBox checkBox = styleCheckboxes.get(style);
                if (checkBox != null) {
                    checkBox.setSelected(true);
                }
            }
            
            // Обновляем заголовок и кнопку
            createEditLabel.setText("Editing style set:");

            saveButton.setText("Refresh");
        }
    }
    
    @FXML
    private void onClearClick() {
        resetEditForm();
    }
    
    private void resetEditForm() {
        currentlyEditing = null;
        nameField.clear();
        styleCheckboxes.values().forEach(cb -> cb.setSelected(false));
        createEditLabel.setText("New set creation:");
        saveButton.setText("Save");
    }
    
    @FXML
    private void onCloseClick() {
        ((Stage) feelingsListView.getScene().getWindow()).close();
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