package org.example.demo.category;

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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Контроллер для экрана управления маппингами категорий и типов
 */
public class CategoryTypeMappingViewController {

    @FXML
    private ListView<CategoryTypeMapping> mappingsListView;

    @FXML
    private TextField categoryField;

    @FXML
    private FlowPane typesFlowPane;

    @FXML
    private Button saveButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button editButton;

    @FXML
    private Label createEditLabel;

    private CategoryTypeMappingService mappingService;
    private Map<ClothingType, CheckBox> typeCheckboxes;
    private CategoryTypeMapping currentlyEditing;

    @FXML
    public void initialize() {
        mappingService = CategoryTypeMappingService.getInstance();
        typeCheckboxes = new HashMap<>();

        // Set up the mappings list
        refreshMappingsListView();

        // Set up cell display in the list
        mappingsListView.setCellFactory(lv -> new ListCell<CategoryTypeMapping>() {
            @Override
            protected void updateItem(CategoryTypeMapping item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Populate the panel with checkboxes for types
        setupTypeCheckboxes();

        // Set up buttons
        removeButton.setDisable(true);
        editButton.setDisable(true);

        // Selection handler
        mappingsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean hasSelection = newSelection != null;
                    removeButton.setDisable(!hasSelection);
                    editButton.setDisable(!hasSelection);
                }
        );

        // Reset the edit form
        resetEditForm();
    }

    private void setupTypeCheckboxes() {
        // Получаем все возможные типы одежды
        EnumSet<ClothingType> types = mappingService.getAllClothingTypes();

        // Очищаем контейнер и карту чекбоксов
        typesFlowPane.getChildren().clear();
        typeCheckboxes.clear();

        // Создаем чекбоксы для каждого типа
        for (ClothingType type : types) {
            CheckBox checkBox = new CheckBox(type.getDisplayName());
            typeCheckboxes.put(type, checkBox);
            typesFlowPane.getChildren().add(checkBox);
        }
    }

    private void refreshMappingsListView() {
        List<CategoryTypeMapping> allMappings = mappingService.getAllMappings();
        ObservableList<CategoryTypeMapping> observableMappings = FXCollections.observableArrayList(allMappings);
        mappingsListView.setItems(observableMappings);
    }

    @FXML
    private void onSaveClick() {
        String category = categoryField.getText().trim();

        if (category.isEmpty()) {
            showAlert("Error", "Category name is required");
            return;
        }

        // Create a list of selected types
        List<ClothingType> selectedTypes = new ArrayList<>();
        for (Map.Entry<ClothingType, CheckBox> entry : typeCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedTypes.add(entry.getKey());
            }
        }

        if (selectedTypes.isEmpty()) {
            showAlert("Error", "You must select at least one type");
            return;
        }

        // Create or update the mapping
        CategoryTypeMapping mapping;
        if (currentlyEditing != null) {
            mapping = currentlyEditing;
            mapping.setCategory(category);
            mapping.setTypes(selectedTypes);
        } else {
            mapping = new CategoryTypeMapping(category, selectedTypes);
        }

        // Save the mapping
        mappingService.addMapping(mapping);

        // Update the list and reset the form
        refreshMappingsListView();
        resetEditForm();

        showInfo("Save", "Category mapping saved successfully");
    }

    @FXML
    private void onRemoveClick() {
        CategoryTypeMapping selectedMapping = mappingsListView.getSelectionModel().getSelectedItem();
        if (selectedMapping != null) {
            mappingService.removeMapping(selectedMapping.getCategory());
            refreshMappingsListView();
            resetEditForm();
            showInfo("Delete", "Category mapping deleted successfully");
        }
    }

    @FXML
    private void onEditClick() {
        CategoryTypeMapping selectedMapping = mappingsListView.getSelectionModel().getSelectedItem();
        if (selectedMapping != null) {
            // Set the current mapping for editing
            currentlyEditing = selectedMapping;

            // Fill the form with data from the selected mapping
            categoryField.setText(selectedMapping.getCategory());

            // Reset all checkboxes
            typeCheckboxes.values().forEach(cb -> cb.setSelected(false));

            // Mark selected types
            for (ClothingType type : selectedMapping.getTypes()) {
                CheckBox checkBox = typeCheckboxes.get(type);
                if (checkBox != null) {
                    checkBox.setSelected(true);
                }
            }

            // Update header and button
            createEditLabel.setText("Edit category mapping:");
            saveButton.setText("Update");
        }
    }

    @FXML
    private void onClearClick() {
        resetEditForm();
    }

    private void resetEditForm() {
        currentlyEditing = null;
        categoryField.clear();
        typeCheckboxes.values().forEach(cb -> cb.setSelected(false));
        createEditLabel.setText("Create new mapping:");
        saveButton.setText("Save");
    }

    @FXML
    private void onCloseClick() {
        ((Stage) mappingsListView.getScene().getWindow()).close();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}