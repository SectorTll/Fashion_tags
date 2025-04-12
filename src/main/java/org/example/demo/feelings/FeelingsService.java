package org.example.demo.feelings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.example.demo.AppSettings;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Сервис для работы с наборами стилей одежды
 */
public class FeelingsService {
    private Feelings feelings;
    private static FeelingsService instance;
    
    private FeelingsService() {
        loadFeelings();
    }
    
    public static FeelingsService getInstance() {
        if (instance == null) {
            instance = new FeelingsService();
        }
        return instance;
    }
    
    public void loadFeelings() {
        String feelingsFile = AppSettings.getInstance().getFeelingsFile();
        if (feelingsFile != null && !feelingsFile.isEmpty()) {
            try {
                File file = new File(feelingsFile);
                if (file.exists()) {
                    feelings = Feelings.loadFromFile(feelingsFile);
                } else {
                    // Если файл не существует, создаем новый экземпляр
                    feelings = new Feelings();
                    // Создаем родительскую директорию
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    // Сохраняем пустой файл
                    saveFeelings();
                }
            } catch (IOException e) {
                feelings = new Feelings();
                showError("Error loading style sets", e.getMessage());

            }
        } else {
            // Если путь не указан, создаем путь по умолчанию
            String userHome = System.getProperty("user.home");
            Path defaultPath = Paths.get(userHome, ".clothesml", "feelings.json");
            AppSettings.getInstance().setFeelingsFile(defaultPath.toString());
            feelings = new Feelings();
            saveFeelings();
        }
    }
    
    public void saveFeelings() {
        String feelingsFile = AppSettings.getInstance().getFeelingsFile();
        if (feelingsFile != null && !feelingsFile.isEmpty()) {
            try {
                feelings.saveToFile(feelingsFile);
            } catch (IOException e) {
                showError("Error saving style sets", e.getMessage());

            }
        }
    }
    
    public void addFeeling(Feeling feeling) {
        feelings.addFeeling(feeling);
        saveFeelings();
    }
    
    public void removeFeeling(String name) {
        feelings.removeFeeling(name);
        saveFeelings();
    }
    
    public List<Feeling> getAllFeelings() {
        return feelings.getFeelings();
    }
    
    public Optional<Feeling> getFeelingByName(String name) {
        return feelings.getByName(name);
    }
    
    /**
     * Получить список всех доступных стилей
     */
    public List<String> getAvailableStyles() {
        return new ArrayList<>(Feelings.AVAILABLE_STYLES);
    }
    
    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}