package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.SelectionMode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.example.demo.wardrobe.WardrobeItem;
import org.example.demo.wardrobe.WardrobeService;

public class ImageDetailsController {
    @FXML
    private ImageView imageView;

    @FXML
    private Label validationLabel;

    @FXML
    private ListView<OutfitItemWrapper> outfitListView;

    @FXML
    private Button addToWardrobeButton;

    private File imageFile;
    private OutfitDetails outfitDetails;
    private WardrobeService wardrobeService;

    // Вспомогательный класс для хранения OutfitItem и флага наличия в гардеробе
    private static class OutfitItemWrapper {
        private final OutfitItem item;
        private final boolean inWardrobe;

        public OutfitItemWrapper(OutfitItem item, boolean inWardrobe) {
            this.item = item;
            this.inWardrobe = inWardrobe;
        }

        public OutfitItem getItem() {
            return item;
        }

        public boolean isInWardrobe() {
            return inWardrobe;
        }

        @Override
        public String toString() {
            StringBuilder itemDesc = new StringBuilder();
            itemDesc.append(item.getCategory().toUpperCase()).append("\n");
            itemDesc.append("Tags: ").append(String.join(", ", item.getTags()));
            return itemDesc.toString();
        }
    }

    public void initialize(File imageFile) {
        this.imageFile = imageFile;
        this.wardrobeService = WardrobeService.getInstance();

        try {
            // Загружаем изображение
            Image image = new Image(new FileInputStream(imageFile));
            imageView.setImage(image);

            // Загружаем JSON с описанием
            String jsonPath = imageFile.getAbsolutePath().replace(".png", ".json");
            File jsonFile = new File(jsonPath);

            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                outfitDetails = mapper.readValue(jsonFile, OutfitDetails.class);

                // Отображаем статус валидации
                validationLabel.setText("Outfit is " +
                        (outfitDetails.isValidOutfit() ? "valid" : "invalid") +
                        (outfitDetails.getErrorMessage().isEmpty() ? "" : ": " + outfitDetails.getErrorMessage()));

                // Создаем обертки для элементов с проверкой наличия в гардеробе
                List<OutfitItemWrapper> wrappedItems = outfitDetails.getOutfit().stream()
                        .map(item -> new OutfitItemWrapper(item, isItemInWardrobe(item, imageFile.getName())))
                        .collect(Collectors.toList());

                outfitListView.setItems(FXCollections.observableArrayList(wrappedItems));

                // Настраиваем отображение ячеек с подсветкой
                outfitListView.setCellFactory(lv -> new ListCell<OutfitItemWrapper>() {
                    @Override
                    protected void updateItem(OutfitItemWrapper item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item.toString());
                            if (item.isInWardrobe()) {
                                setStyle("-fx-background-color: #D1F0D1;"); // Светло-зеленый фон
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });

                // Включаем множественный выбор
                outfitListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            } else {
                validationLabel.setText("Description file not found");
                addToWardrobeButton.setDisable(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            validationLabel.setText("Error loading data");
            addToWardrobeButton.setDisable(true);
        }
    }

    private boolean isItemInWardrobe(OutfitItem outfitItem, String fileName) {
        // Получаем все элементы гардероба
        List<WardrobeItem> wardrobeItems = wardrobeService.getAllItems();

        // Ищем совпадение по категории, тегам и файлу
        return wardrobeItems.stream().anyMatch(wardrobeItem ->
                wardrobeItem.getCategory().equals(outfitItem.getCategory()) &&
                        wardrobeItem.getFileName().equals(fileName) &&
                        wardrobeItemContainsAllTags(wardrobeItem, outfitItem.getTags())
        );
    }

    private boolean wardrobeItemContainsAllTags(WardrobeItem wardrobeItem, List<String> tags) {
        // Проверяем, что элемент гардероба содержит все теги
        return wardrobeItem.getTags() != null &&
                wardrobeItem.getTags().containsAll(tags) &&
                wardrobeItem.getTags().size() == tags.size();
    }

    @FXML
    private void onAddToWardrobeClick() {
        // Проверяем, что у нас есть данные и есть выбранные элементы
        if (outfitDetails == null || outfitListView.getSelectionModel().getSelectedItems().isEmpty()) {
            showAlert("Ошибка", "Выберите элементы для добавления в гардероб");
            return;
        }

        // Получаем выбранные элементы
        ObservableList<OutfitItemWrapper> selectedItems = outfitListView.getSelectionModel().getSelectedItems();

        // Счетчик добавленных элементов
        int addedCount = 0;

        // Добавляем каждый выбранный элемент в гардероб
        for (OutfitItemWrapper wrapper : selectedItems) {
            // Пропускаем элементы, которые уже в гардеробе
            if (wrapper.isInWardrobe()) {
                continue;
            }

            OutfitItem outfitItem = wrapper.getItem();

            // Создаем новый элемент гардероба
            WardrobeItem wardrobeItem = new WardrobeItem(
                    outfitItem.getCategory(),
                    new ArrayList<>(outfitItem.getTags()),
                    imageFile.getName()
            );

            // Добавляем в гардероб
            wardrobeService.addItem(wardrobeItem);
            addedCount++;
        }

        if (addedCount == 0) {
            showAlert("Информация", "Выбранные элементы уже добавлены в гардероб");
        } else {
            showSuccess("Добавлено в гардероб", "Добавлено " + addedCount +
                    " элемент" + (addedCount == 1 ? "" : (addedCount < 5 ? "а" : "ов")) + " в гардероб.");

            // Обновляем отображение для отражения изменений
            initialize(imageFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Предупреждение");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}