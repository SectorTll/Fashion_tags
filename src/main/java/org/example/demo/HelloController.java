package org.example.demo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.scene.input.MouseButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.example.demo.category.CategoryTypeMapping;
import org.example.demo.category.CategoryTypeMappingService;
import org.example.demo.category.CategoryTypeSelectorController;
import org.example.demo.feelings.Feeling;
import org.example.demo.feelings.FeelingsService;
import org.example.demo.feelings.FeelingsViewController;
import org.example.demo.wardrobe.WardrobeItem;
import org.example.demo.wardrobe.WardrobeService;
import org.example.demo.weather.Weather;
import org.example.demo.weather.WeatherService;
import org.example.demo.weather.WeatherViewController;

public class HelloController {
    @FXML
    private GridPane imageGrid;

    @FXML
    private ProgressBar loadingProgress;

    @FXML
    private Label statusLabel;

    @FXML
    private ScrollPane imageScrollPane;

    @FXML
    private Label totalItemsLabel;

    @FXML
    private Label totalAssociationsLabel;



    private List<ImageInfo> currentImages = new ArrayList<>();
    private static final int THUMBNAIL_SIZE = 100;
    private static final int SPACING = 10; // Отступ между картинками

    /**
     * Модификация initialize для добавления проверки немаппированных категорий
     */
    @FXML
    public void initialize() {
        // Слушаем изменение размера ScrollPane
        imageScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!currentImages.isEmpty()) {
                displayImages(currentImages);
            }
        });


        updateWardrobeInfo();

        loadImagesFromSavedDirectory();

        // Добавляем регистрацию контроллера
        registerControllerInRoot();

        // Проверяем немаппированные категории при запуске
        Platform.runLater(this::checkUnmappedCategories);

    }

    private void updateWardrobeInfo() {

        WardrobeService wardrobeService = WardrobeService.getInstance();
        int totalItems = wardrobeService.getAllItems().size();
        totalItemsLabel.setText(String.valueOf(totalItems));


        TagAssociationsStorage tagAssociationsStorage = TagAssociationsStorage.getInstance();
        int totalAssociations = tagAssociationsStorage.getAllAssociations().size();
        totalAssociationsLabel.setText(String.valueOf(totalAssociations));
    }


    public void refreshImages() {

        updateWardrobeInfo();


        if (!currentImages.isEmpty()) {
            displayImages(currentImages);
        }
    }

    /**
     * Метод для инициализации текущего контроллера как свойства корневого элемента
     * Вызывается после инициализации контроллера
     */
    private void registerControllerInRoot() {
        Platform.runLater(() -> {
            if (imageGrid != null && imageGrid.getScene() != null &&
                    imageGrid.getScene().getRoot() instanceof Parent) {
                Parent root = (Parent) imageGrid.getScene().getRoot();
                root.getProperties().put("controller", this);
            }
        });
    }

    @FXML
    protected void onSelectFolderClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        // Используем текущую директорию как начальную, если она установлена
        String currentDir = AppSettings.getInstance().getImagesDirectory();
        if (!currentDir.isEmpty()) {
            File initialDir = new File(currentDir);
            if (initialDir.exists()) {
                directoryChooser.setInitialDirectory(initialDir);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(imageGrid.getScene().getWindow());

        if (selectedDirectory != null) {
            // Сохраняем выбранную директорию в настройках
            AppSettings.getInstance().setImagesDirectory(selectedDirectory.getAbsolutePath());
            loadImagesInBackground(selectedDirectory);
        }
    }


    @FXML
    private void onViewWardrobeClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("wardrobe-view.fxml"));
            Parent root = loader.load();

            Stage wardrobeStage = new Stage();
            wardrobeStage.setTitle("Wardrobe");
            wardrobeStage.setScene(new Scene(root));
            wardrobeStage.initModality(Modality.WINDOW_MODAL);
            wardrobeStage.initOwner(imageGrid.getScene().getWindow());


            int initialWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            List<String> initialCategories = new ArrayList<>(WardrobeService.getInstance().getAllCategories());

            wardrobeStage.showAndWait();


            int newWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            List<String> newCategories = WardrobeService.getInstance().getAllCategories();


            updateWardrobeInfo();

            if (newWardrobeSize != initialWardrobeSize && !currentImages.isEmpty()) {

                displayImages(currentImages);
            }

            // Проверяем, появились ли новые категории
            boolean hasNewCategories = false;
            for (String category : newCategories) {
                if (!initialCategories.contains(category)) {
                    hasNewCategories = true;
                    break;
                }
            }


            if (hasNewCategories || newWardrobeSize > initialWardrobeSize) {
                checkUnmappedCategories();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSettingsMenuClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings-view.fxml"));
            Parent root = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Настройки");
            settingsStage.setScene(new Scene(root));
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.initOwner(imageGrid.getScene().getWindow());

            SettingsController controller = loader.getController();
            controller.setStage(settingsStage);

            settingsStage.showAndWait();

            if (controller.isSaveClicked()) {
                // Перезагружаем изображения из новой директории
                String newDir = AppSettings.getInstance().getImagesDirectory();
                if (!newDir.isEmpty()) {
                    loadImagesInBackground(new File(newDir));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImagesFromSavedDirectory() {
        String savedDir = AppSettings.getInstance().getImagesDirectory();
        if (!savedDir.isEmpty()) {
            File directory = new File(savedDir);
            if (directory.exists()) {
                loadImagesInBackground(directory);
            }
        }
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(
            Math.max(25, Runtime.getRuntime().availableProcessors() - 1)
    );

    /**
     * Сбрасывает счетчик проблемных изображений
     */
    private void resetIssuesCount() {
        if (totalIssuesLabel != null) {
            totalIssuesLabel.setText("0");
        }

        // Также сбрасываем флаги проблем во всех изображениях
        if (currentImages != null) {
            for (ImageInfo info : currentImages) {
                info.hasIssues = false;
                info.issueDetails.clear();
            }
        }
    }

    private void loadImagesInBackground(File directory) {
        resetIssuesCount();

        Task<List<ImageInfo>> loadTask = new Task<>() {
            @Override
            protected List<ImageInfo> call() throws Exception {
                File[] files = directory.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".png")
                );

                if (files == null) return new ArrayList<>();

                int total = files.length;
                int updateInterval = 10;
                AtomicInteger processedCount = new AtomicInteger(0);
                updateMessage("Loading images: 0/" + total);

                // Создаем список CompletableFuture для каждого изображения
                List<CompletableFuture<ImageInfo>> futures = Arrays.stream(files)
                        .map(file -> CompletableFuture.supplyAsync(() -> {
                            try {
                                Image image = new Image(new FileInputStream(file),
                                        THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);

                                // Обновляем прогресс
                                int currentCount = processedCount.incrementAndGet();
                                if (currentCount % updateInterval == 0 || currentCount == total) {
                                    updateProgress(currentCount, total);
                                    updateMessage("Loading images: " + currentCount + "/" + total);
                                }

                                return new ImageInfo(image, file);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }, executorService))
                        .collect(Collectors.toList());

                // Ждем завершения всех загрузок
                List<ImageInfo> images = futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                return images;
            }
        };

        loadingProgress.progressProperty().bind(loadTask.progressProperty());
        statusLabel.textProperty().bind(loadTask.messageProperty());
        loadingProgress.setVisible(true);

        loadTask.setOnSucceeded(event -> {
            List<ImageInfo> images = loadTask.getValue();
            displayImages(images);
            loadingProgress.setVisible(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Images loaded: " + images.size());
            Platform.runLater(this::onCheckAssociationsClick);
        });

        loadTask.setOnFailed(event -> {
            loadingProgress.setVisible(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Error loading images");

            event.getSource().getException().printStackTrace();
        });

        new Thread(loadTask).start();
    }

    // Добавляем метод для очистки ресурсов при закрытии приложения
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private void displayImages(List<ImageInfo> images) {
        Platform.runLater(() -> {
            currentImages = images;
            imageGrid.getChildren().clear();


            double viewportWidth = imageScrollPane.getViewportBounds().getWidth();
            int columnsCount = Math.max(1, (int)((viewportWidth - SPACING) / (THUMBNAIL_SIZE + SPACING)));


            WardrobeService wardrobeService = WardrobeService.getInstance();
            List<String> filesInWardrobe = wardrobeService.getAllItems().stream()
                    .map(WardrobeItem::getFileName)
                    .distinct()
                    .collect(Collectors.toList());

            // Получаем список файлов с ассоциациями для подсветки
            TagAssociationsStorage tagAssociationsStorage = TagAssociationsStorage.getInstance();
            List<String> filesWithAssociations = tagAssociationsStorage.getAllAssociations().stream()
                    .map(TagAssociation::getImageFileName)
                    .distinct()
                    .collect(Collectors.toList());

            int col = 0;
            int row = 0;

            for (ImageInfo imageInfo : images) {
                ImageView imageView = new ImageView(imageInfo.image);
                imageView.setFitWidth(THUMBNAIL_SIZE);
                imageView.setFitHeight(THUMBNAIL_SIZE);


                boolean inWardrobe = filesInWardrobe.contains(imageInfo.file.getName());


                boolean hasAssociations = filesWithAssociations.contains(imageInfo.file.getName());


                VBox containerBox = new VBox();
                containerBox.setAlignment(javafx.geometry.Pos.CENTER);
                containerBox.setSpacing(0);

                // Верхняя полоса (синяя) - для ассоциаций
                Region topBar = new Region();
                topBar.setPrefHeight(5);
                topBar.setPrefWidth(THUMBNAIL_SIZE);
                topBar.setVisible(hasAssociations);
                topBar.setStyle("-fx-background-color: #3498DB;");


                Region bottomBar = new Region();
                bottomBar.setPrefHeight(5);
                bottomBar.setPrefWidth(THUMBNAIL_SIZE);
                bottomBar.setVisible(inWardrobe);
                bottomBar.setStyle("-fx-background-color: #2ECC71;");

                // Добавляем все элементы в контейнер
                containerBox.getChildren().addAll(topBar, imageView, bottomBar);

                // Настраиваем стиль окантовки в зависимости от наличия проблем
                if (imageInfo.hasIssues) {
                    containerBox.setStyle("-fx-border-color: #FF0000; -fx-border-width: 2;");

                    // Создаем текст тултипа с описанием проблем
                    StringBuilder tooltipText = new StringBuilder("Association issues:\n");

                    for (String issue : imageInfo.issueDetails) {
                        tooltipText.append("• ").append(issue).append("\n");
                    }

                    // Добавляем тултип
                    Tooltip tooltip = new Tooltip(tooltipText.toString());
                    tooltip.setWrapText(true);
                    tooltip.setMaxWidth(300);
                    Tooltip.install(containerBox, tooltip);
                } else {
                    containerBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1;");
                }

                containerBox.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.PRIMARY) {
                        // Левая кнопка мыши - показываем детали
                        showFullImage(imageInfo.file);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        // Правая кнопка мыши - открываем окно управления тегами
                        showTagManager(imageInfo.file);
                    }
                });

                imageGrid.add(containerBox, col, row);

                col++;
                if (col == columnsCount) {
                    col = 0;
                    row++;
                }
            }
        });
    }

    private void showTagManager(File file) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tag-manager-view.fxml"));
            Parent root = loader.load();

            TagManagerController controller = loader.getController();
            controller.initialize(file);

            // Устанавливаем ссылку на главный контроллер
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Tags management: " + file.getName());
            stage.setScene(new Scene(root));
            stage.initOwner(imageGrid.getScene().getWindow());

            // Показываем окно
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error opening the tag management window");

            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }



    @FXML
    private void onManageFeelingsClick() {
        try {
            // Используем класс FeelingsViewController для правильного определения пути к FXML
            FXMLLoader loader = new FXMLLoader(FeelingsViewController.class.getResource("/org/example/demo/feelings-view.fxml"));
            Parent root = loader.load();

            Stage feelingsStage = new Stage();
            feelingsStage.setTitle("Управление наборами стилей");
            feelingsStage.setScene(new Scene(root));
            feelingsStage.initModality(Modality.WINDOW_MODAL);
            feelingsStage.initOwner(imageGrid.getScene().getWindow());

            feelingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the style sets management window: " + e.getMessage());

        }
    }
    @FXML
    private void onManageWeatherClick() {
        try {
            // Используем класс WeatherViewController для правильного определения пути к FXML
            FXMLLoader loader = new FXMLLoader(WeatherViewController.class.getResource("/org/example/demo/weather-view.fxml"));
            Parent root = loader.load();

            // Получаем доступ к контроллеру после загрузки FXML
            WeatherViewController controller = loader.getController();

            Stage weatherStage = new Stage();
            weatherStage.setTitle("Weather management");
            weatherStage.setScene(new Scene(root));
            weatherStage.initModality(Modality.WINDOW_MODAL);
            weatherStage.initOwner(imageGrid.getScene().getWindow());

            weatherStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open the weather management window: " + e.getMessage());

        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private Label totalIssuesLabel;

    @FXML
    private void onCheckAssociationsClick() {
        // Если нет загруженных изображений, выводим сообщение
        if (currentImages.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Association Check",
                    "No images loaded",
                    "Please load images before checking associations.");
            return;
        }

        // Получаем все ассоциации
        TagAssociationsStorage tagAssociationsStorage = TagAssociationsStorage.getInstance();
        List<TagAssociation> associations = tagAssociationsStorage.getAllAssociations();

        if (associations.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Association Check",
                    "No associations to check",
                    "The association database is empty.");

            return;
        }


        WardrobeService wardrobeService = WardrobeService.getInstance();
        List<WardrobeItem> wardrobeItems = wardrobeService.getAllItems();


        WeatherService weatherService = WeatherService.getInstance();
        List<Weather> weatherConditions = weatherService.getAllWeatherConditions();


        FeelingsService feelingsService = FeelingsService.getInstance();
        List<Feeling> feelings = feelingsService.getAllFeelings();


        for (ImageInfo imageInfo : currentImages) {
            imageInfo.hasIssues = false;
            imageInfo.issueDetails.clear();
        }

        // Счетчик проблемных изображений
        int issueCount = 0;

        // Проверяем каждую ассоциацию
        for (TagAssociation association : associations) {
            String imageName = association.getImageFileName();

            // Находим ImageInfo для данного изображения
            ImageInfo targetInfo = null;
            for (ImageInfo info : currentImages) {
                if (info.file.getName().equals(imageName)) {
                    targetInfo = info;
                    break;
                }
            }

            if (targetInfo == null) {
                // Изображение не загружено, пропускаем
                continue;
            }

            boolean imageHasIssue = false;

            // Проверка тегов
            for (TagItem tagItem : association.getReferenceTagItems()) {
                boolean tagFound = false;
                for (WardrobeItem wardrobeItem : wardrobeItems) {
                    if (wardrobeItem.getCategory().equals(tagItem.getCategory()) &&
                            wardrobeItem.getTags().containsAll(tagItem.getTags()) &&
                            tagItem.getTags().containsAll(wardrobeItem.getTags())) {
                        tagFound = true;
                        break;
                    }
                }

                if (!tagFound) {
                    targetInfo.addIssue("Tag is not found in wardrobe: " + tagItem.toString());
                    imageHasIssue = true;
                }
            }

            // Проверка погоды
            Weather weather = association.getWeather();
            if (weather != null && weather.getCondition() != null && !weather.getCondition().isEmpty()) {
                boolean weatherFound = false;
                for (Weather w : weatherConditions) {
                    if (w.getCondition().equals(weather.getCondition()) &&
                            w.getTemperature() == weather.getTemperature()) {
                        weatherFound = true;
                        break;
                    }
                }

                if (!weatherFound) {
                    targetInfo.addIssue("Weather conditions not found: " + weather.toString());

                    imageHasIssue = true;
                }
            }

            // Проверка набора стилей
            Feeling feeling = association.getFeeling();
            if (feeling != null && feeling.getName() != null && !feeling.getName().isEmpty()) {
                boolean feelingFound = false;
                for (Feeling f : feelings) {
                    if (f.getName().equals(feeling.getName())) {
                        feelingFound = true;
                        break;
                    }
                }

                if (!feelingFound) {
                    targetInfo.addIssue("Набор стилей не найден: " + feeling.getName());
                    imageHasIssue = true;
                }
            }

            if (imageHasIssue) {
                issueCount++;
            }
        }

        // Обновляем интерфейс
        totalIssuesLabel.setText(String.valueOf(issueCount));

        // Перерисовываем сетку изображений для отображения проблем
        displayImages(currentImages);

        // Показываем сообщение о завершении проверки
        showAlert(Alert.AlertType.INFORMATION, "Association Check",
                "Check completed",
                "Found issues: " + issueCount);
    }

    /**
     * Показывает стандартное диалоговое окно с сообщением
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void onManageCategoryTypesClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/category-mapping-view.fxml"));
            Parent root = loader.load();

            Stage categoryTypesStage = new Stage();
            categoryTypesStage.setTitle("Categories types management");
            categoryTypesStage.setScene(new Scene(root));
            categoryTypesStage.initModality(Modality.WINDOW_MODAL);
            categoryTypesStage.initOwner(imageGrid.getScene().getWindow());

            categoryTypesStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to open categories types management window: " + e.getMessage());
        }
    }

    private static class ImageInfo {
        Image image;
        File file;
        boolean hasIssues;      // Флаг наличия проблем с ассоциациями
        List<String> issueDetails; // Детали проблем для отображения в тултипе

        ImageInfo(Image image, File file) {
            this.image = image;
            this.file = file;
            this.hasIssues = false;
            this.issueDetails = new ArrayList<>();
        }

        public void addIssue(String issue) {
            this.hasIssues = true;
            this.issueDetails.add(issue);
        }
    }

    private void showFullImage(File file) {
        try {

            int initialWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            List<String> initialCategories = new ArrayList<>(WardrobeService.getInstance().getAllCategories());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("image-details-view.fxml"));
            Parent root = loader.load();

            ImageDetailsController controller = loader.getController();
            controller.initialize(file);

            Stage stage = new Stage();
            stage.setTitle(file.getName());
            stage.setScene(new Scene(root));
            stage.initOwner(imageGrid.getScene().getWindow());


            stage.showAndWait();


            int newWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            List<String> newCategories = WardrobeService.getInstance().getAllCategories();


            updateWardrobeInfo();

            if (newWardrobeSize != initialWardrobeSize && !currentImages.isEmpty()) {

                displayImages(currentImages);
            }


            boolean hasNewCategories = false;
            for (String category : newCategories) {
                if (!initialCategories.contains(category)) {
                    hasNewCategories = true;
                    break;
                }
            }



            if (hasNewCategories || newWardrobeSize > initialWardrobeSize) {
                checkUnmappedCategories();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void checkUnmappedCategories() {

        CategoryTypeMappingService mappingService = CategoryTypeMappingService.getInstance();


        WardrobeService wardrobeService = WardrobeService.getInstance();


        List<String> allCategories = wardrobeService.getAllCategories();


        List<String> unmappedCategories = new ArrayList<>();


        for (String category : allCategories) {

            Optional<CategoryTypeMapping> mapping = mappingService.getMappingByCategory(category);
            if (mapping.isEmpty() || mapping.get().getTypes().isEmpty()) {
                unmappedCategories.add(category);
            }
        }


        if (!unmappedCategories.isEmpty()) {
            boolean shouldProceed = showConfirmationDialog(
                    "Categories without types detected",
                    "Categories found in wardrobe that aren't assigned to clothing types",
                    "Do you want to add types for " + unmappedCategories.size() +
                            " categor" + (unmappedCategories.size() == 1 ? "y" : "ies") + "?"
            );

            if (shouldProceed) {
                showCategoryMappingDialog(unmappedCategories);
            }
        }
    }


    private boolean showConfirmationDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


    private void showCategoryMappingDialog(List<String> categories) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("category-type-selector-view.fxml"));
            Parent root = loader.load();


            CategoryTypeSelectorController controller = loader.getController();


            controller.initialize(categories);


            Stage dialogStage = new Stage();
            dialogStage.setTitle("Category Type Selection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(imageGrid.getScene().getWindow());
            dialogStage.setScene(new Scene(root));


            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to open type selection dialog",
                    e.getMessage());

        }
    }
}