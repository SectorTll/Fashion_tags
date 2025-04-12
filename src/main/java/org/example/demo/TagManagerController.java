package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.layout.HBox;
import javafx.scene.control.Label;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.demo.category.CategoryTypeMappingService;
import org.example.demo.category.ClothingType;
import org.example.demo.wardrobe.WardrobeItem;
import org.example.demo.wardrobe.WardrobeService;
import org.example.demo.feelings.Feeling;
import org.example.demo.feelings.FeelingsService;
import org.example.demo.weather.Weather;
import org.example.demo.weather.WeatherService;

public class TagManagerController {

    @FXML
    private ImageView sourceImageView;

    @FXML
    private ListView<String> sourceTagsListView;

    @FXML
    private ListView<TagItem> selectedTagsListView;

    @FXML
    private ListView<WardrobeItemWrapper> wardrobeTagsListView;

    @FXML
    private ListView<Weather> weatherListView;

    @FXML
    private ListView<Feeling> feelingListView;

    @FXML
    private ComboBox<String> categoryFilter;

    @FXML
    private TextField tagFilter;

    private File imageFile;
    private OutfitDetails outfitDetails;
    private WardrobeService wardrobeService;
    private TagAssociationsStorage tagAssociationsStorage;
    private FeelingsService feelingsService;
    private WeatherService weatherService;

    private Weather selectedWeather;
    private Feeling selectedFeeling;

    private ObservableList<WardrobeItemWrapper> allWardrobeItems;
    private FilteredList<WardrobeItemWrapper> filteredWardrobeItems;
    private ObservableList<TagItem> selectedTags;
    private ObservableList<Weather> weatherItems;
    private ObservableList<Feeling> feelingItems;
    private CategoryTypeMappingService categoryTypeMappingService = CategoryTypeMappingService.getInstance();

    // Ссылка на главный контроллер
    private HelloController mainController;

    /**
     * Устанавливает ссылку на главный контроллер
     *
     * @param controller экземпляр HelloController
     */
    public void setMainController(HelloController controller) {
        this.mainController = controller;
    }

    /**
     * Обновляет отображение изображений на главном экране
     */
    private void refreshMainView() {
        if (mainController != null) {
            mainController.refreshImages();
        }
    }


    // Вспомогательный класс для элементов гардероба
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

    public void initialize(File imageFile) {
        try {
            this.imageFile = imageFile;
            this.wardrobeService = WardrobeService.getInstance();
            this.feelingsService = FeelingsService.getInstance();
            this.weatherService = WeatherService.getInstance();

            // Инициализируем хранилище ассоциаций
            try {
                this.tagAssociationsStorage = TagAssociationsStorage.getInstance();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error initializing the association store: " + e.getMessage());
                // Создаем пустое хранилище в случае ошибки
                this.tagAssociationsStorage = null;
            }

            // Загружаем изображение и его детали
            loadImageAndDetails();

            // Загружаем теги из гардероба
            loadWardrobeItems();

            // Загружаем списки погоды и стилей
            loadWeatherAndFeelingLists();

            // Загружаем сохраненные ассоциации для этого изображения
            loadSavedAssociations();

            // Настраиваем фильтры
            setupFilters();

            // Настраиваем отображение выбранных тегов
            selectedTagsListView.setCellFactory(lv -> new ListCell<TagItem>() {
                @Override
                protected void updateItem(TagItem item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                    }
                }
            });
            selectedTagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            // Настраиваем обработчики двойного клика
            setupDoubleClickHandlers();

            // Настраиваем обработчики выбора погоды и стилей
            setupWeatherAndFeelingSelection();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Initialization Error", "Failed to initialize the window: " + e.getMessage());

        }
    }

    private void loadImageAndDetails() {
        try {
            // Загружаем изображение
            Image image = new Image(new FileInputStream(imageFile));
            sourceImageView.setImage(image);

            // Загружаем JSON с описанием
            String jsonPath = imageFile.getAbsolutePath().replace(".png", ".json");
            File jsonFile = new File(jsonPath);

            if (jsonFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                outfitDetails = mapper.readValue(jsonFile, OutfitDetails.class);

                // Заполняем список исходных тегов
                List<String> sourceTags = new ArrayList<>();
                for (OutfitItem item : outfitDetails.getOutfit()) {
                    String tagLine = item.getCategory().toUpperCase() + ": " +
                            String.join(", ", item.getTags());
                    sourceTags.add(tagLine);
                }

                sourceTagsListView.setItems(FXCollections.observableArrayList(sourceTags));

            } else {
                showAlert("Error", "Description file not found: " + jsonPath);

            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load data: " + e.getMessage());

        }
    }

    private void loadWardrobeItems() {
        List<WardrobeItem> items = wardrobeService.getAllItems();
        List<WardrobeItemWrapper> wrappers = items.stream()
                .map(WardrobeItemWrapper::new)
                .collect(Collectors.toList());

        allWardrobeItems = FXCollections.observableArrayList(wrappers);
        filteredWardrobeItems = new FilteredList<>(allWardrobeItems, p -> true);
        wardrobeTagsListView.setItems(filteredWardrobeItems);

        // Настраиваем отображение ячеек с миниатюрами
        wardrobeTagsListView.setCellFactory(lv -> new ListCell<WardrobeItemWrapper>() {
            private final HBox container = new HBox(5); // 5 - отступ между элементами
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();

            {
                // Настраиваем размеры ImageView
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);

                // Добавляем стиль для выделения изображения при наведении курсора
                imageView.setStyle("-fx-cursor: hand;");

                // Настраиваем стиль метки
                label.setWrapText(true);

                // Добавляем компоненты в контейнер
                container.getChildren().addAll(imageView, label);
                container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(WardrobeItemWrapper item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    // Очищаем обработчики клика
                    imageView.setOnMouseClicked(null);
                } else {
                    // Устанавливаем текст тегов
                    label.setText(item.toString());

                    // Устанавливаем изображение, если оно доступно
                    if (item.hasThumbnail()) {
                        imageView.setImage(item.getThumbnailImage());
                        imageView.setVisible(true);

                        // Добавляем обработчик клика по изображению
                        final String fileName = item.getItem().getFileName();
                        imageView.setOnMouseClicked(event -> {
                            showFullImageFromWardrobe(fileName);
                            event.consume(); // Предотвращаем дальнейшее распространение события
                        });
                    } else {
                        imageView.setVisible(false);
                        imageView.setOnMouseClicked(null);
                    }

                    setGraphic(container);
                }
            }
        });

        // Включаем множественный выбор
        wardrobeTagsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    private void loadWeatherAndFeelingLists() {
        // Загружаем список погоды
        List<Weather> weatherList = weatherService.getAllWeatherConditions();
        weatherItems = FXCollections.observableArrayList(weatherList);
        weatherListView.setItems(weatherItems);

        // Настраиваем отображение элементов погоды
        weatherListView.setCellFactory(lv -> new ListCell<Weather>() {
            @Override
            protected void updateItem(Weather item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    if (item.equals(selectedWeather)) {
                        setStyle("-fx-background-color: #b3e0ff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Загружаем список стилей
        List<Feeling> feelingList = feelingsService.getAllFeelings();
        feelingItems = FXCollections.observableArrayList(feelingList);
        feelingListView.setItems(feelingItems);

        // Настраиваем отображение элементов стилей
        feelingListView.setCellFactory(lv -> new ListCell<Feeling>() {
            @Override
            protected void updateItem(Feeling item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getName() + " (" + String.join(", ", item.getStyles()) + ")");
                    if (item.equals(selectedFeeling)) {
                        setStyle("-fx-background-color: #b3e0ff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void setupWeatherAndFeelingSelection() {
        // Обработчик выбора погоды
        weatherListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedWeather = newVal;
                refreshWeatherAndFeelingLists();
            }
        });

        // Обработчик выбора стиля
        feelingListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedFeeling = newVal;
                refreshWeatherAndFeelingLists();
            }
        });
    }

    private void refreshWeatherAndFeelingLists() {
        // Обновляем выделение элементов в списках
        weatherListView.refresh();
        feelingListView.refresh();
    }

    private void setupDoubleClickHandlers() {
        // Обработчик двойного клика для списка исходных тегов
        sourceTagsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedTag = sourceTagsListView.getSelectionModel().getSelectedItem();
                if (selectedTag != null) {
                    // Парсим строку тега и создаем TagItem
                    addSourceTagToSelected(selectedTag);
                }
            }
        });

        // Обработчик двойного клика для списка тегов гардероба
        wardrobeTagsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                WardrobeItemWrapper selectedItem = wardrobeTagsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    addWardrobeItemToSelected(selectedItem);
                }
            }
        });

        // Обработчик двойного клика для списка выбранных тегов
        selectedTagsListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TagItem selectedItem = selectedTagsListView.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    selectedTags.remove(selectedItem);
                }
            }
        });
    }

    // Метод для добавления тега из исходного списка в список выбранных
    private void addSourceTagToSelected(String tagString) {
        try {
            // Парсим строку тега (формат: "CATEGORY: tag1, tag2, tag3")
            int colonIndex = tagString.indexOf(":");
            if (colonIndex > 0) {
                String category = tagString.substring(0, colonIndex).trim().toUpperCase();
                String tagsStr = tagString.substring(colonIndex + 1).trim();
                String[] tagsArray = tagsStr.split(",");

                List<String> tags = new ArrayList<>();
                for (String tag : tagsArray) {
                    tags.add(tag.trim());
                }

                // Создаем новый TagItem
                TagItem tagItem = new TagItem(category, tags);

                // Проверяем, не добавлен ли уже такой тег
                if (!containsTagItem(tagItem)) {
                    selectedTags.add(tagItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error parsing tag: " + tagString);

        }
    }

    // Метод для добавления элемента гардероба в список выбранных
    private void addWardrobeItemToSelected(WardrobeItemWrapper wrapper) {
        WardrobeItem wardrobeItem = wrapper.getItem();

        // Создаем новый TagItem из WardrobeItem
        TagItem tagItem = new TagItem(
                wardrobeItem.getCategory(),
                new ArrayList<>(wardrobeItem.getTags())
        );

        // Проверяем, не добавлен ли уже такой тег
        if (!containsTagItem(tagItem)) {
            selectedTags.add(tagItem);
        }
    }

    /**
     * Отображает полноразмерное изображение в новом окне
     *
     * @param imageFileName имя файла изображения
     */
    private void showFullImageFromWardrobe(String imageFileName) {
        try {
            String imagesDirectory = AppSettings.getInstance().getImagesDirectory();
            if (imagesDirectory == null || imagesDirectory.isEmpty()) {
                showAlert("Error", "Image directory not specified");
                return;
            }

            File imageFile = new File(imagesDirectory, imageFileName);
            if (!imageFile.exists()) {
                showAlert("Error", "Image file is not found: " + imageFile.getAbsolutePath());
                return;
            }

            // Создаем окно предпросмотра
            Stage previewStage = new Stage();
            previewStage.setTitle("View: " + imageFileName);
            previewStage.initModality(Modality.WINDOW_MODAL);
            previewStage.initOwner(sourceImageView.getScene().getWindow());

            // Загружаем изображение с учетом максимальных размеров для отображения
            int maxWidth = 800;
            int maxHeight = 600;

            // Используем кеш для получения изображения большего размера
            Image image = ThumbnailCache.getInstance().getThumbnail(imageFileName, maxWidth, maxHeight);

            if (image == null) {
                showAlert("Error", "Image loading failed");
                return;
            }

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);

            // Создаем контейнер и сцену
            VBox root = new VBox(10, imageView); // 10 - отступ
            root.setStyle("-fx-background-color: black; -fx-padding: 10;");
            root.setAlignment(javafx.geometry.Pos.CENTER);

            // Добавляем кнопку закрытия
            Button closeButton = new Button("Close");
            closeButton.setOnAction(e -> previewStage.close());
            root.getChildren().add(closeButton);

            Scene scene = new Scene(root);

            previewStage.setScene(scene);
            previewStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Image loading failed: " + e.getMessage());
        }
    }

    private void loadSavedAssociations() {
        selectedTags = FXCollections.observableArrayList();

        // Проверяем, что хранилище ассоциаций доступно
        if (tagAssociationsStorage != null) {
            try {
                // Ищем сохраненные ассоциации для этого изображения
                Optional<TagAssociation> savedAssociation =
                        tagAssociationsStorage.getAssociationForImage(imageFile.getName());

                if (savedAssociation.isPresent()) {
                    TagAssociation association = savedAssociation.get();

                    // Добавляем сохраненные эталонные теги в список выбранных
                    selectedTags.addAll(association.getReferenceTagItems());

                    // Загружаем сохраненную погоду
                    selectedWeather = association.getWeather();
                    if (selectedWeather != null && selectedWeather.getCondition() != null && !selectedWeather.getCondition().isEmpty()) {
                        // Выбираем соответствующий элемент в списке погоды
                        for (Weather weather : weatherItems) {
                            if (weather.getCondition().equals(selectedWeather.getCondition()) &&
                                    weather.getTemperature() == selectedWeather.getTemperature()) {
                                weatherListView.getSelectionModel().select(weather);
                                break;
                            }
                        }
                    }

                    // Загружаем сохраненные стили
                    selectedFeeling = association.getFeeling();
                    if (selectedFeeling != null && selectedFeeling.getName() != null && !selectedFeeling.getName().isEmpty()) {
                        // Выбираем соответствующий элемент в списке стилей
                        for (Feeling feeling : feelingItems) {
                            if (feeling.getName().equals(selectedFeeling.getName())) {
                                feelingListView.getSelectionModel().select(feeling);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading associations: " + e.getMessage());
            }
        }

        selectedTagsListView.setItems(selectedTags);
        refreshWeatherAndFeelingLists();
    }

    private void setupFilters() {
        // Заполняем выпадающий список категорий
        List<String> categories = wardrobeService.getAllCategories();
        categories.add(0, "All"); // Добавляем опцию "Все"
        categoryFilter.setItems(FXCollections.observableArrayList(categories));
        categoryFilter.getSelectionModel().selectFirst();

        // Настраиваем обработчик изменения фильтров
        categoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tagFilter.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void applyFilters() {
        String selectedCategory = categoryFilter.getValue();
        String tagFilterText = tagFilter.getText().trim().toLowerCase();

        filteredWardrobeItems.setPredicate(wrapper -> {
            WardrobeItem item = wrapper.getItem();

            // Проверяем фильтр категории
            boolean matchesCategory = "All".equals(selectedCategory) ||
                    item.getCategory().equals(selectedCategory);

            // Проверяем фильтр тегов
            boolean matchesTag = tagFilterText.isEmpty() ||
                    item.getTags().stream()
                            .anyMatch(tag -> tag.toLowerCase().contains(tagFilterText));

            return matchesCategory && matchesTag;
        });
    }

    @FXML
    private void onResetFiltersClick() {
        categoryFilter.getSelectionModel().selectFirst();
        tagFilter.clear();
    }

    @FXML
    private void onAddSelectedClick() {
        ObservableList<WardrobeItemWrapper> selectedItems =
                wardrobeTagsListView.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            showAlert("Warning", "You haven't selected any wardrobe items");
            return;
        }

        // Добавляем выбранные элементы в список тегов
        for (WardrobeItemWrapper wrapper : selectedItems) {
            WardrobeItem wardrobeItem = wrapper.getItem();

            // Создаем новый TagItem из WardrobeItem
            TagItem tagItem = new TagItem(
                    wardrobeItem.getCategory(),
                    new ArrayList<>(wardrobeItem.getTags())
            );

            // Проверяем, не добавлен ли уже такой тег
            if (!containsTagItem(tagItem)) {
                selectedTags.add(tagItem);
            }
        }
    }

    @FXML
    private void onRemoveSelectedClick() {
        ObservableList<TagItem> selectedItems =
                selectedTagsListView.getSelectionModel().getSelectedItems();

        if (selectedItems.isEmpty()) {
            showAlert("Warning", "No items selected for removal");
            return;
        }

        // Создаем копию списка выбранных элементов для безопасного удаления
        List<TagItem> itemsToRemove = new ArrayList<>(selectedItems);

        // Удаляем выбранные элементы из списка
        selectedTags.removeAll(itemsToRemove);
    }

    @FXML
    private void onSaveTagAssociationsClick() {
        try {
            // Check if the associations storage is available
            if (tagAssociationsStorage == null) {
                throw new Exception("Associations storage is unavailable. Please check application settings.");
            }

            // Check if weather and style are selected
            selectedWeather = weatherListView.getSelectionModel().getSelectedItem();
            selectedFeeling = feelingListView.getSelectionModel().getSelectedItem();

            // Check that weather is selected
            if (selectedWeather == null) {
                showAlert("Error", "Please select weather conditions.");
                return;
            }

            // Check that style is selected
            if (selectedFeeling == null) {
                showAlert("Error", "Please select a style set (feeling).");
                return;
            }

            // Get list of wardrobe items
            List<WardrobeItem> wardrobeItems = wardrobeService.getAllItems();

            boolean hasTorso = false;
            boolean hasLegs = false;
            boolean hasHips = false;

            // Check that all selected tags are present in the wardrobe
            for (TagItem tagItem : selectedTags) {
                boolean foundInWardrobe = false;
                for (WardrobeItem wardrobeItem : wardrobeItems) {
                    if (wardrobeItem.getCategory().equals(tagItem.getCategory()) &&
                            wardrobeItem.getTags().containsAll(tagItem.getTags()) &&
                            tagItem.getTags().containsAll(wardrobeItem.getTags())) {
                        foundInWardrobe = true;
                        break;
                    }
                }

                if (!foundInWardrobe) {
                    showAlert("Error", "Tag '" + tagItem.toString() + "' not found in wardrobe. " +
                            "All selected tags must be present in the wardrobe.");
                    return;
                }

                var t = categoryTypeMappingService.getTypesForCategory(tagItem.getCategory());
                for (var clType : t) {
                    if (clType.equals(ClothingType.TORSO))
                        hasTorso = true;

                    if (clType.equals(ClothingType.LEGS))
                        hasLegs = true;

                    if (clType.equals(ClothingType.HIPS))
                        hasHips = true;
                }
            }

            if (!hasTorso ) {
                showAlert("Error", "Following parts is not covered. Torso: " + hasTorso + ", Legs: " + hasLegs + ", Hips: " + hasHips);
                return;
            }


            // Create a new association for the current image
            TagAssociation association = new TagAssociation(imageFile.getName());

            // Add original image tags
            if (outfitDetails != null && outfitDetails.getOutfit() != null) {
                for (OutfitItem outfitItem : outfitDetails.getOutfit()) {
                    TagItem originalTag = new TagItem(
                            outfitItem.getCategory(),
                            new ArrayList<>(outfitItem.getTags())
                    );
                    association.addOriginalTagItem(originalTag);
                }
            }

            // Add all selected reference tags
            for (TagItem tagItem : selectedTags) {
                association.addReferenceTagItem(tagItem);
            }

            // Add selected weather
            association.setWeather(selectedWeather);

            // Add selected style set
            association.setFeeling(selectedFeeling);

            // Save association
            tagAssociationsStorage.saveAssociation(association);

            // Update image display on main screen
            refreshMainView();

            // Show success message
            showInfo("Success", "Associations saved successfully.");

            // Close window
            Stage stage = (Stage) sourceImageView.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save associations: " + e.getMessage());
        }
    }

    /**
     * Проверяет, содержит ли список выбранных тегов такой же элемент
     */
    private boolean containsTagItem(TagItem tagItem) {
        return selectedTags.stream().anyMatch(item ->
                item.getCategory().equals(tagItem.getCategory()) &&
                        item.getTags().containsAll(tagItem.getTags()) &&
                        tagItem.getTags().containsAll(item.getTags()));
    }

    @FXML
    private void onCloseClick() {
        ((Stage) sourceImageView.getScene().getWindow()).close();
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