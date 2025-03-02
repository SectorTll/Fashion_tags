package org.example.demo.feelings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер для окна выбора набора стилей
 */
public class FeelingSelectorController {

    @FXML
    private ComboBox<String> feelingComboBox;

    @FXML
    private ListView<String> stylesListView;

    private FeelingsService feelingsService;
    private Feeling selectedFeeling;
    private boolean selectionConfirmed = false;

    @FXML
    public void initialize() {
        feelingsService = FeelingsService.getInstance();

        // Загружаем имена наборов стилей в комбобокс
        refreshFeelingsComboBox();

        // Добавляем обработчик выбора набора стилей
        feelingComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                loadSelectedFeelingStyles(newValue);
            } else {
                stylesListView.getItems().clear();
            }
        });
    }

    /**
     * Обновляет комбобокс с наборами стилей
     */
    private void refreshFeelingsComboBox() {
        List<String> feelingNames = feelingsService.getAllFeelings().stream()
                .map(Feeling::getName)
                .collect(Collectors.toList());

        feelingComboBox.setItems(FXCollections.observableArrayList(feelingNames));

        // Если есть наборы, выбираем первый
        if (!feelingNames.isEmpty()) {
            feelingComboBox.getSelectionModel().selectFirst();
        }
    }

    /**
     * Загружает стили выбранного набора
     */
    private void loadSelectedFeelingStyles(String feelingName) {
        Optional<Feeling> feeling = feelingsService.getFeelingByName(feelingName);

        if (feeling.isPresent()) {
            // Сохраняем выбранный набор
            selectedFeeling = feeling.get();

            // Отображаем стили в списке
            stylesListView.setItems(FXCollections.observableArrayList(selectedFeeling.getStyles()));
        } else {
            stylesListView.getItems().clear();
            selectedFeeling = null;
        }
    }

    /**
     * Открывает окно управления наборами стилей
     */
    @FXML
    private void onManageFeelingsClick() {
        try {
            // Запоминаем текущий выбор
            String currentSelection = feelingComboBox.getValue();

            // Загружаем окно управления наборами
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/feelings-view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Управление наборами стилей");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(feelingComboBox.getScene().getWindow());

            stage.showAndWait();

            // Обновляем комбобокс
            refreshFeelingsComboBox();

            // Восстанавливаем выбор, если возможно
            if (currentSelection != null && feelingComboBox.getItems().contains(currentSelection)) {
                feelingComboBox.setValue(currentSelection);
            } else if (!feelingComboBox.getItems().isEmpty()) {
                feelingComboBox.getSelectionModel().selectFirst();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Подтверждает выбор набора стилей
     */
    @FXML
    private void onSelectClick() {
        if (selectedFeeling != null) {
            selectionConfirmed = true;
            ((Stage) feelingComboBox.getScene().getWindow()).close();
        }
    }

    /**
     * Отменяет выбор набора стилей
     */
    @FXML
    private void onCancelClick() {
        selectedFeeling = null;
        selectionConfirmed = false;
        ((Stage) feelingComboBox.getScene().getWindow()).close();
    }

    /**
     * Возвращает выбранный набор стилей
     */
    public Feeling getSelectedFeeling() {
        return selectionConfirmed ? selectedFeeling : null;
    }

    /**
     * Статический метод для открытия диалога выбора набора стилей
     */
    public static Optional<Feeling> showAndWait(Stage ownerStage) {
        try {
            FXMLLoader loader = new FXMLLoader(FeelingSelectorController.class.getResource("/org/example/demo/feeling-selector-view.fxml"));
            Parent root = loader.load();

            FeelingSelectorController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Выбор набора стилей");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(ownerStage);

            stage.showAndWait();

            return Optional.ofNullable(controller.getSelectedFeeling());

        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}