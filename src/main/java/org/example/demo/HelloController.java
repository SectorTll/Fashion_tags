package org.example.demo;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.example.demo.feelings.FeelingsViewController;
import org.example.demo.wardrobe.WardrobeItem;
import org.example.demo.wardrobe.WardrobeService;
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
     * Модификация initialize для включения регистрации контроллера
     */
    @FXML
    public void initialize() {
        // Слушаем изменение размера ScrollPane
        imageScrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (!currentImages.isEmpty()) {
                displayImages(currentImages);
            }
        });

        // Обновляем информацию о гардеробе
        updateWardrobeInfo();

        loadImagesFromSavedDirectory();

        // Добавляем регистрацию контроллера
        registerControllerInRoot();
    }

    private void updateWardrobeInfo() {
        // Обновляем информацию о гардеробе
        WardrobeService wardrobeService = WardrobeService.getInstance();
        int totalItems = wardrobeService.getAllItems().size();
        totalItemsLabel.setText(String.valueOf(totalItems));

        // Обновляем информацию о связях
        TagAssociationsStorage tagAssociationsStorage = TagAssociationsStorage.getInstance();
        int totalAssociations = tagAssociationsStorage.getAllAssociations().size();
        totalAssociationsLabel.setText(String.valueOf(totalAssociations));
    }


    public void refreshImages() {
        // Обновляем информацию о гардеробе
        updateWardrobeInfo();

        // Обновляем отображение изображений
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
            wardrobeStage.setTitle("Гардероб");
            wardrobeStage.setScene(new Scene(root));
            wardrobeStage.initModality(Modality.WINDOW_MODAL);
            wardrobeStage.initOwner(imageGrid.getScene().getWindow());

            // Сохраняем текущее количество элементов в гардеробе
            int initialWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            wardrobeStage.showAndWait();

            // Проверяем, изменился ли размер гардероба
            int newWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            // Обновляем информацию о гардеробе в любом случае
            updateWardrobeInfo();

            if (newWardrobeSize != initialWardrobeSize && !currentImages.isEmpty()) {
                // Обновляем отображение изображений для обновления подсветки
                displayImages(currentImages);
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

    private void loadImagesInBackground(File directory) {
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
                updateMessage("Загрузка изображений: 0/" + total);

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
                                    updateMessage("Загрузка изображений: " + currentCount + "/" + total);
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
            statusLabel.setText("Загружено изображений: " + images.size());
        });

        loadTask.setOnFailed(event -> {
            loadingProgress.setVisible(false);
            statusLabel.textProperty().unbind();
            statusLabel.setText("Ошибка загрузки изображений");
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

            // Вычисляем количество колонок в зависимости от ширины окна
            double viewportWidth = imageScrollPane.getViewportBounds().getWidth();
            int columnsCount = Math.max(1, (int)((viewportWidth - SPACING) / (THUMBNAIL_SIZE + SPACING)));

            // Получаем список файлов из гардероба для подсветки
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

                // Определяем, есть ли изображение в гардеробе
                boolean inWardrobe = filesInWardrobe.contains(imageInfo.file.getName());

                // Определяем, есть ли для изображения сохраненные ассоциации тегов
                boolean hasAssociations = filesWithAssociations.contains(imageInfo.file.getName());

                // Создаем структуру контейнера для отображения цветных полос
                VBox containerBox = new VBox();
                containerBox.setAlignment(javafx.geometry.Pos.CENTER);
                containerBox.setSpacing(0);

                // Верхняя полоса (синяя) - для ассоциаций
                Region topBar = new Region();
                topBar.setPrefHeight(5);
                topBar.setPrefWidth(THUMBNAIL_SIZE);
                topBar.setVisible(hasAssociations);
                topBar.setStyle("-fx-background-color: #3498DB;");

                // Нижняя полоса (зеленая) - для гардероба
                Region bottomBar = new Region();
                bottomBar.setPrefHeight(5);
                bottomBar.setPrefWidth(THUMBNAIL_SIZE);
                bottomBar.setVisible(inWardrobe);
                bottomBar.setStyle("-fx-background-color: #2ECC71;");

                // Добавляем все элементы в контейнер
                containerBox.getChildren().addAll(topBar, imageView, bottomBar);

                // Добавляем рамку вокруг всего контейнера
                containerBox.setStyle("-fx-border-color: #CCCCCC; -fx-border-width: 1;");

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
            stage.setTitle("Управление тегами: " + file.getName());
            stage.setScene(new Scene(root));
            stage.initOwner(imageGrid.getScene().getWindow());

            // Показываем окно
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Ошибка открытия окна управления тегами");
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
            showAlert("Ошибка", "Не удалось открыть окно управления наборами стилей: " + e.getMessage());
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
            weatherStage.setTitle("Управление погодными условиями");
            weatherStage.setScene(new Scene(root));
            weatherStage.initModality(Modality.WINDOW_MODAL);
            weatherStage.initOwner(imageGrid.getScene().getWindow());

            weatherStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно управления погодными условиями: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class ImageInfo {
        Image image;
        File file;

        ImageInfo(Image image, File file) {
            this.image = image;
            this.file = file;
        }
    }

    private void showFullImage(File file) {
        try {
            // Сохраняем текущее количество элементов в гардеробе
            int initialWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("image-details-view.fxml"));
            Parent root = loader.load();

            ImageDetailsController controller = loader.getController();
            controller.initialize(file);

            Stage stage = new Stage();
            stage.setTitle(file.getName());
            stage.setScene(new Scene(root));
            stage.initOwner(imageGrid.getScene().getWindow());

            // Показываем окно и ждем его закрытия
            stage.showAndWait();

            // Проверяем, изменился ли размер гардероба
            int newWardrobeSize = WardrobeService.getInstance().getAllItems().size();

            // Обновляем информацию о гардеробе в любом случае
            updateWardrobeInfo();

            if (newWardrobeSize != initialWardrobeSize && !currentImages.isEmpty()) {
                // Обновляем отображение изображений для обновления подсветки
                displayImages(currentImages);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}