package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.TabPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.Random;

import org.example.demo.wardrobe.WardrobeItem;
import org.example.demo.wardrobe.WardrobeService;

public class WardrobeViewController {
    @FXML
    private ListView<WardrobeItemWrapper> itemsListView;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private TextField tagFilter;

    @FXML
    private Button removeItemButton;

    @FXML
    private TabPane statisticsTabPane;

    @FXML
    private GridPane categoriesGrid;

    @FXML
    private GridPane tagsGrid;

    @FXML
    private TilePane tagCloudPane;

    private ObservableList<WardrobeItemWrapper> allItems;
    private FilteredList<WardrobeItemWrapper> filteredItems;
    private WardrobeService wardrobeService;
    private String imagesDirectory;
    private Random random = new Random();

    // Вспомогательный класс для элементов гардероба с миниатюрами
    private static class WardrobeItemWrapper {
        private final WardrobeItem item;
        private Image thumbnailImage;

        public WardrobeItemWrapper(WardrobeItem item) {
            this.item = item;
            loadThumbnail();
        }

        private void loadThumbnail() {
            try {
                String imageFileName = item.getFileName();
                if (imageFileName != null && !imageFileName.isEmpty()) {
                    // Используем кеш для получения миниатюры
                    thumbnailImage = ThumbnailCache.getInstance().getThumbnail(imageFileName);
                }
            } catch (Exception e) {
                // Если не удалось загрузить изображение, просто продолжаем без него
                e.printStackTrace();
            }
        }

        public WardrobeItem getItem() {
            return item;
        }

        public Image getThumbnailImage() {
            return thumbnailImage;
        }

        public boolean hasThumbnail() {
            return thumbnailImage != null;
        }

        @Override
        public String toString() {
            StringBuilder itemDesc = new StringBuilder();
            itemDesc.append(item.getCategory().toUpperCase()).append(": ");
            itemDesc.append(String.join(", ", item.getTags()));
            return itemDesc.toString();
        }
    }

    @FXML
    public void initialize() {
        wardrobeService = WardrobeService.getInstance();
        imagesDirectory = AppSettings.getInstance().getImagesDirectory();

        // Загружаем данные
        loadData();

        // Настраиваем фильтры
        setupFilters();

        // Настраиваем кнопку удаления
        removeItemButton.setDisable(true);
        itemsListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> removeItemButton.setDisable(newSelection == null)
        );

        // Добавляем двойной клик для просмотра изображения
        itemsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                WardrobeItemWrapper selectedItem = itemsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    showImagePreview(selectedItem.getItem().getFileName());
                }
            }
        });

        // Загружаем статистику
        loadStatistics();
    }

    private void loadData() {
        List<WardrobeItem> items = wardrobeService.getAllItems();
        List<WardrobeItemWrapper> wrappers = items.stream()
                .map(WardrobeItemWrapper::new)
                .collect(Collectors.toList());

        allItems = FXCollections.observableArrayList(wrappers);
        filteredItems = new FilteredList<>(allItems, p -> true);
        itemsListView.setItems(filteredItems);

        // Настраиваем отображение ячеек с миниатюрами
        itemsListView.setCellFactory(lv -> new ListCell<WardrobeItemWrapper>() {
            private final HBox container = new HBox(10); // Отступ между элементами
            private final ImageView imageView = new ImageView();
            private final VBox textContainer = new VBox(5); // Отступ между строками текста
            private final Label categoryLabel = new Label();
            private final Label tagsLabel = new Label();
            private final Label fileNameLabel = new Label();

            {
                // Настраиваем размеры ImageView
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);

                // Настраиваем стили текста
                categoryLabel.setStyle("-fx-font-weight: bold;");
                tagsLabel.setWrapText(true);
                fileNameLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");

                // Добавляем текстовые компоненты в вертикальный контейнер
                textContainer.getChildren().addAll(categoryLabel, tagsLabel, fileNameLabel);

                // Настраиваем основной контейнер
                container.getChildren().addAll(imageView, textContainer);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(WardrobeItemWrapper item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Устанавливаем данные элемента
                    WardrobeItem wardrobeItem = item.getItem();
                    categoryLabel.setText(wardrobeItem.getCategory().toUpperCase());
                    tagsLabel.setText(String.join(", ", wardrobeItem.getTags()));
                    fileNameLabel.setText(wardrobeItem.getFileName());

                    // Устанавливаем изображение
                    if (item.hasThumbnail()) {
                        imageView.setImage(item.getThumbnailImage());
                        imageView.setVisible(true);
                    } else {
                        imageView.setVisible(false);
                    }

                    setGraphic(container);
                }
            }
        });
    }

    private void loadStatistics() {
        // Получаем статистику категорий
        Map<String, Long> categoryStats = wardrobeService.getCategoryStatistics();

        // Получаем статистику тегов
        Map<String, Long> tagStats = wardrobeService.getTagStatistics();

        // Отображаем статистику категорий
        displayCategoryStatistics(categoryStats);

        // Отображаем статистику тегов
        displayTagStatistics(tagStats);

        // Создаем облако тегов
        createTagCloud(tagStats);
    }

    private void displayCategoryStatistics(Map<String, Long> categoryStats) {
        categoriesGrid.getChildren().clear();

        // Добавляем заголовки
        Label categoryHeader = new Label("Категория");
        categoryHeader.setStyle("-fx-font-weight: bold;");
        Label countHeader = new Label("Количество");
        countHeader.setStyle("-fx-font-weight: bold;");

        categoriesGrid.add(categoryHeader, 0, 0);
        categoriesGrid.add(countHeader, 1, 0);

        // Сортируем категории по количеству предметов (по убыванию)
        List<Map.Entry<String, Long>> sortedEntries = categoryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Добавляем строки с данными
        int row = 1;
        for (Map.Entry<String, Long> entry : sortedEntries) {
            Label categoryLabel = new Label(entry.getKey());
            Label countLabel = new Label(entry.getValue().toString());
            countLabel.setAlignment(Pos.CENTER_RIGHT);

            categoriesGrid.add(categoryLabel, 0, row);
            categoriesGrid.add(countLabel, 1, row);
            row++;
        }
    }

    private void displayTagStatistics(Map<String, Long> tagStats) {
        tagsGrid.getChildren().clear();

        // Добавляем заголовки
        Label tagHeader = new Label("Тег");
        tagHeader.setStyle("-fx-font-weight: bold;");
        Label countHeader = new Label("Количество");
        countHeader.setStyle("-fx-font-weight: bold;");

        tagsGrid.add(tagHeader, 0, 0);
        tagsGrid.add(countHeader, 1, 0);

        // Сортируем теги по количеству использований (по убыванию)
        List<Map.Entry<String, Long>> sortedEntries = tagStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Добавляем строки с данными
        int row = 1;
        for (Map.Entry<String, Long> entry : sortedEntries) {
            Label tagLabel = new Label(entry.getKey());
            Label countLabel = new Label(entry.getValue().toString());
            countLabel.setAlignment(Pos.CENTER_RIGHT);

            tagsGrid.add(tagLabel, 0, row);
            tagsGrid.add(countLabel, 1, row);
            row++;
        }
    }

    private void createTagCloud(Map<String, Long> tagStats) {
        tagCloudPane.getChildren().clear();

        // Находим максимальное и минимальное количество для масштабирования
        long maxCount = tagStats.values().stream()
                .max(Comparator.naturalOrder())
                .orElse(1L);

        long minCount = tagStats.values().stream()
                .min(Comparator.naturalOrder())
                .orElse(1L);

        // Сортируем теги по количеству использований (по убыванию)
        List<Map.Entry<String, Long>> sortedEntries = tagStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toList());

        // Создаем облако тегов
        for (Map.Entry<String, Long> entry : sortedEntries) {
            // Вычисляем размер шрифта в зависимости от частоты
            double fontSize = calculateFontSize(entry.getValue(), minCount, maxCount);

            // Создаем метку для тега
            Label tagLabel = new Label(entry.getKey());
            tagLabel.setStyle(String.format("-fx-font-size: %.1fpx; -fx-font-weight: %s;",
                    fontSize, fontSize > 14 ? "bold" : "normal"));

            // Случайный цвет для фона (светлый)
            Color bgColor = getRandomPastelColor();
            tagLabel.setBackground(new Background(new BackgroundFill(
                    bgColor, new CornerRadii(5), Insets.EMPTY)));
            tagLabel.setPadding(new Insets(5, 10, 5, 10));

            // Добавляем метку в облако
            tagCloudPane.getChildren().add(tagLabel);
        }
    }

    private double calculateFontSize(long count, long minCount, long maxCount) {
        // Масштабируем размер шрифта от 10 до 24 в зависимости от частоты
        double minSize = 10.0;
        double maxSize = 24.0;

        if (maxCount == minCount) {
            return (minSize + maxSize) / 2;
        }

        return minSize + (count - minCount) * (maxSize - minSize) / (maxCount - minCount);
    }

    private Color getRandomPastelColor() {
        // Создаем случайный пастельный цвет
        double r = 0.7 + random.nextDouble() * 0.3;
        double g = 0.7 + random.nextDouble() * 0.3;
        double b = 0.7 + random.nextDouble() * 0.3;

        return new Color(r, g, b, 1.0);
    }

    private void setupFilters() {
        // Заполняем выпадающий список категорий
        List<String> categories = wardrobeService.getAllCategories();
        categories.add(0, "Все категории"); // Добавляем опцию "Все"
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
        categoryFilter.getSelectionModel().selectFirst();

        // Настраиваем обработчик изменения фильтров
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tagFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String selectedCategory = categoryFilter.getValue();
        String tagFilterText = tagFilter.getText().trim().toLowerCase();

        filteredItems.setPredicate(wrapper -> {
            WardrobeItem item = wrapper.getItem();

            // Проверяем фильтр категории
            boolean matchesCategory = "Все категории".equals(selectedCategory) ||
                    item.getCategory().equals(selectedCategory);

            // Проверяем фильтр тегов
            boolean matchesTag = tagFilterText.isEmpty() ||
                    item.getTags().stream()
                            .anyMatch(tag -> tag.toLowerCase().contains(tagFilterText));

            return matchesCategory && matchesTag;
        });
    }

    private void showImagePreview(String fileName) {
        if (imagesDirectory == null || imagesDirectory.isEmpty()) {
            showAlert("Ошибка", "Не указана директория с изображениями");
            return;
        }

        File imageFile = new File(imagesDirectory, fileName);
        if (!imageFile.exists()) {
            showAlert("Ошибка", "Файл изображения не найден: " + imageFile.getAbsolutePath());
            return;
        }

        try {
            // Создаем окно предпросмотра
            Stage previewStage = new Stage();
            previewStage.setTitle("Просмотр: " + fileName);
            previewStage.initModality(Modality.WINDOW_MODAL);
            previewStage.initOwner(itemsListView.getScene().getWindow());

            // Загружаем изображение с учетом максимальных размеров для отображения
            int maxWidth = 800;
            int maxHeight = 600;

            // Используем кеш для получения изображения большего размера
            Image image = ThumbnailCache.getInstance().getThumbnail(fileName, maxWidth, maxHeight);

            if (image == null) {
                // Если кеш не вернул изображение, пробуем загрузить его напрямую
                image = new Image(new FileInputStream(imageFile));
            }

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            // Ограничиваем размер изображения
            if (image.getWidth() > maxWidth) {
                imageView.setFitWidth(maxWidth);
            }
            if (image.getHeight() > maxHeight) {
                imageView.setFitHeight(maxHeight);
            }

            // Создаем контейнер и сцену
            VBox root = new VBox(10, imageView); // 10 - отступ
            root.setStyle("-fx-background-color: black; -fx-padding: 10;");
            root.setAlignment(javafx.geometry.Pos.CENTER);

            // Добавляем кнопку закрытия
            Button closeButton = new Button("Закрыть");
            closeButton.setOnAction(e -> previewStage.close());
            root.getChildren().add(closeButton);

            Scene scene = new Scene(root);
            previewStage.setScene(scene);
            previewStage.show();

        } catch (FileNotFoundException e) {
            showAlert("Ошибка", "Не удалось загрузить изображение: " + e.getMessage());
        }
    }

    @FXML
    private void onRemoveClick() {
        WardrobeItemWrapper selectedWrapper = itemsListView.getSelectionModel().getSelectedItem();
        if (selectedWrapper != null) {
            wardrobeService.removeItem(selectedWrapper.getItem());
            loadData();
            setupFilters();
            loadStatistics();

            showInfo("Удаление", "Элемент успешно удален из гардероба");
        }
    }

    @FXML
    private void onResetFiltersClick() {
        categoryFilter.getSelectionModel().selectFirst();
        tagFilter.clear();
    }

    @FXML
    private void onCloseClick() {
        Stage stage = (Stage) itemsListView.getScene().getWindow();
        stage.close();
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