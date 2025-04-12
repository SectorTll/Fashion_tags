package org.example.demo.category;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
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
 * Контроллер для диалога выбора типов для категорий
 */
public class CategoryTypeSelectorController {

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private VBox typesContainer;

    @FXML
    private ListView<String> savedMappingsListView;

    @FXML
    private Button saveButton;

    private CategoryTypeMappingService mappingService;
    private Map<ClothingType, CheckBox> typeCheckboxes;
    private List<String> categories;
    private ObservableList<String> savedMappings;

    /**
     * Инициализация с списком немаппированных категорий
     */
    public void initialize(List<String> unmappedCategories) {
        this.categories = new ArrayList<>(unmappedCategories);
        this.mappingService = CategoryTypeMappingService.getInstance();
        this.typeCheckboxes = new HashMap<>();
        this.savedMappings = FXCollections.observableArrayList();

        // Настраиваем комбобокс категорий
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        // Выбираем первую категорию, если есть
        if (!categories.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
        }

        // Создаем чекбоксы для типов
        setupTypeCheckboxes();

        // Слушаем изменения выбранной категории
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTypeCheckboxes(newVal);
            }
        });

        // Настраиваем список сохраненных маппингов
        savedMappingsListView.setItems(savedMappings);

        // Загружаем чекбоксы для текущей категории
        if (!categories.isEmpty()) {
            updateTypeCheckboxes(categoryComboBox.getValue());
        } else {
            saveButton.setDisable(true);
        }
    }

    /**
     * Создает чекбоксы для всех типов одежды
     */
    private void setupTypeCheckboxes() {
        // Получаем все типы одежды
        EnumSet<ClothingType> types = mappingService.getAllClothingTypes();

        // Очищаем контейнер и карту чекбоксов
        typesContainer.getChildren().clear();
        typeCheckboxes.clear();

        // Создаем чекбоксы для каждого типа
        for (ClothingType type : types) {
            CheckBox checkBox = new CheckBox(type.getDisplayName());
            typeCheckboxes.put(type, checkBox);
            typesContainer.getChildren().add(checkBox);
        }
    }

    /**
     * Обновляет состояние чекбоксов в соответствии с выбранной категорией
     */
    private void updateTypeCheckboxes(String category) {
        // Сначала сбрасываем все чекбоксы
        typeCheckboxes.values().forEach(cb -> cb.setSelected(false));

        // Получаем существующий маппинг для категории, если есть
        Optional<CategoryTypeMapping> mapping = mappingService.getMappingByCategory(category);

        // Если маппинг существует, отмечаем соответствующие типы
        if (mapping.isPresent()) {
            List<ClothingType> types = mapping.get().getTypes();
            for (ClothingType type : types) {
                CheckBox checkBox = typeCheckboxes.get(type);
                if (checkBox != null) {
                    checkBox.setSelected(true);
                }
            }
        }
    }

    /**
     * Сохраняет выбранные типы для текущей категории
     */
    @FXML
    private void onSaveClick() {
        String category = categoryComboBox.getValue();

        if (category == null || category.isEmpty()) {
            showAlert("Error", "Select a category");
            return;
        }

        // Собираем выбранные типы
        List<ClothingType> selectedTypes = new ArrayList<>();
        for (Map.Entry<ClothingType, CheckBox> entry : typeCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedTypes.add(entry.getKey());
            }
        }

        if (selectedTypes.isEmpty()) {
            showAlert("Error", "Select at least one type for the category");
            return;
        }

        // Создаем или обновляем маппинг
        CategoryTypeMapping mapping = new CategoryTypeMapping(category, selectedTypes);
        mappingService.addMapping(mapping);

        // Добавляем в список сохраненных
        savedMappings.add(mapping.toString());

        // Удаляем категорию из списка немаппированных
        categories.remove(category);

        // Обновляем комбобокс
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        // Если еще есть немаппированные категории, выбираем следующую
        if (!categories.isEmpty()) {
            categoryComboBox.getSelectionModel().selectFirst();
            updateTypeCheckboxes(categoryComboBox.getValue());
        } else {
            // Если все категории обработаны, отключаем кнопку и отображаем сообщение
            categoryComboBox.setValue("");
            saveButton.setDisable(true);
            showInfo("Done", "All categories have been assigned to clothing types");
        }
    }

    /**
     * Пропускает текущую категорию
     */
    @FXML
    private void onSkipClick() {
        String category = categoryComboBox.getValue();

        if (category != null && !category.isEmpty()) {
            // Удаляем категорию из списка немаппированных
            categories.remove(category);

            // Обновляем комбобокс
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));

            // Если еще есть немаппированные категории, выбираем следующую
            if (!categories.isEmpty()) {
                categoryComboBox.getSelectionModel().selectFirst();
                updateTypeCheckboxes(categoryComboBox.getValue());
            } else {
                // Если все категории обработаны, отключаем кнопку
                categoryComboBox.setValue("");
                saveButton.setDisable(true);
                showInfo("Done", "All categories have been processed");
            }
        }
    }

    /**
     * Закрывает диалог
     */
    @FXML
    private void onCloseClick() {
        ((Stage) categoryComboBox.getScene().getWindow()).close();
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