package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class SettingsController {
    @FXML
    private TextField directoryField;

    @FXML
    private TextField wardrobeField;

    @FXML
    private TextField weatherField;

    @FXML
    private TextField feelingsField;

    @FXML
    private TextField tagAssociationsField;

    @FXML
    private TextField categoryTypeMappingsField;

    private Stage stage;
    private AppSettings settings;
    private boolean saveClicked = false;

    // In the initialize() method we initialize the new field
    @FXML
    public void initialize() {
        settings = AppSettings.getInstance();
        directoryField.setText(settings.getImagesDirectory());
        wardrobeField.setText(settings.getWardrobeFile());
        weatherField.setText(settings.getWeatherFile());
        feelingsField.setText(settings.getFeelingsFile());
        tagAssociationsField.setText(settings.getTagAssociationsFile());
        categoryTypeMappingsField.setText(settings.getCategoryTypeMappingsFile());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void onBrowseTagAssociationsClick() {
        browseJsonFile("Select the tag associations file (tag_associations.json)", tagAssociationsField);
    }

    @FXML
    private void onBrowseCategoryTypeMappingsClick() {
        browseJsonFile("Select the category type mappings file (category_types.json)", categoryTypeMappingsField);
    }

    @FXML
    private void onSaveClick() {
        settings.setImagesDirectory(directoryField.getText());
        settings.setWardrobeFile(wardrobeField.getText());
        settings.setWeatherFile(weatherField.getText());
        settings.setFeelingsFile(feelingsField.getText());
        settings.setTagAssociationsFile(tagAssociationsField.getText());
        settings.setCategoryTypeMappingsFile(categoryTypeMappingsField.getText());
        saveClicked = true;
        stage.close();
    }

    @FXML
    private void onBrowseDirectoryClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select the image directory");

        String currentDir = directoryField.getText();
        if (!currentDir.isEmpty()) {
            File initialDir = new File(currentDir);
            if (initialDir.exists()) {
                directoryChooser.setInitialDirectory(initialDir);
            }
        }

        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            directoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    private void onBrowseWardrobeClick() {
        browseJsonFile("Select wardrobe file", wardrobeField);
    }

    @FXML
    private void onBrowseWeatherClick() {
        browseJsonFile("Select weather file (weather.json)", weatherField);
    }

    @FXML
    private void onBrowseFeelingsClick() {
        browseJsonFile("Select the feelings file (feelings.json)", feelingsField);
    }

    private void browseJsonFile(String title, TextField targetField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        String currentFile = targetField.getText();
        if (!currentFile.isEmpty()) {
            File file = new File(currentFile);
            if (file.exists()) {
                fileChooser.setInitialDirectory(file.getParentFile());
                fileChooser.setInitialFileName(file.getName());
            }
        }

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            targetField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onCancelClick() {
        stage.close();
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }
}
