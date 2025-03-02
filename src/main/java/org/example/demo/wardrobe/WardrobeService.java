package org.example.demo.wardrobe;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.example.demo.AppSettings;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class WardrobeService {
    private Wardrobe wardrobe;
    private static WardrobeService instance;

    private WardrobeService() {
        loadWardrobe();
    }

    public static WardrobeService getInstance() {
        if (instance == null) {
            instance = new WardrobeService();
        }
        return instance;
    }

    public void loadWardrobe() {
        String wardrobeFile = AppSettings.getInstance().getWardrobeFile();
        if (wardrobeFile != null && !wardrobeFile.isEmpty()) {
            try {
                wardrobe = Wardrobe.loadFromFile(wardrobeFile);
            } catch (IOException e) {
                wardrobe = new Wardrobe();
                showError("Ошибка загрузки гардероба", e.getMessage());
            }
        } else {
            wardrobe = new Wardrobe();
        }
    }

    public void saveWardrobe() {
        String wardrobeFile = AppSettings.getInstance().getWardrobeFile();
        if (wardrobeFile != null && !wardrobeFile.isEmpty()) {
            try {
                wardrobe.saveToFile(wardrobeFile);
            } catch (IOException e) {
                showError("Ошибка сохранения гардероба", e.getMessage());
            }
        }
    }

    public void addItem(WardrobeItem item) {
        wardrobe.addItem(item);
        saveWardrobe();
    }

    public void removeItem(WardrobeItem item) {
        wardrobe.removeItem(item);
        saveWardrobe();
    }

    public List<WardrobeItem> getAllItems() {
        return wardrobe.getItems();
    }

    public List<String> getAllCategories() {
        return wardrobe.getAllCategories();
    }

    public List<String> getAllTags() {
        return wardrobe.getAllTags();
    }

    public Map<String, Long> getCategoryStatistics() {
        return wardrobe.getCategoryStatistics();
    }

    public Map<String, Long> getTagStatistics() {
        return wardrobe.getTagStatistics();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}