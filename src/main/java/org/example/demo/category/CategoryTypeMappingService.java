package org.example.demo.category;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.example.demo.AppSettings;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Service for working with category and clothing type mappings
 */
public class CategoryTypeMappingService {
    private CategoryTypeMappings mappings;
    private static CategoryTypeMappingService instance;

    private CategoryTypeMappingService() {
        loadMappings();
    }

    public static CategoryTypeMappingService getInstance() {
        if (instance == null) {
            instance = new CategoryTypeMappingService();
        }
        return instance;
    }

    /**
     * Loads mappings from file
     */
    public void loadMappings() {
        String mappingsFile = AppSettings.getInstance().getCategoryTypeMappingsFile();
        if (mappingsFile != null && !mappingsFile.isEmpty()) {
            try {
                File file = new File(mappingsFile);
                if (file.exists()) {
                    mappings = CategoryTypeMappings.loadFromFile(mappingsFile);
                } else {
                    // If the file doesn't exist, create a new instance with default mappings
                    mappings = CategoryTypeMappings.createDefaultMappings();
                    // Create parent directory
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    // Save file with default mappings
                    saveMappings();
                }
            } catch (IOException e) {
                mappings = CategoryTypeMappings.createDefaultMappings();
                showError("Error loading category mappings", e.getMessage());
            }
        } else {
            // If path is not specified, create a default path
            String userHome = System.getProperty("user.home");
            Path defaultPath = Paths.get(userHome, ".clothesml", "category_types.json");
            AppSettings.getInstance().setCategoryTypeMappingsFile(defaultPath.toString());
            mappings = CategoryTypeMappings.createDefaultMappings();
            saveMappings();
        }
    }

    /**
     * Saves mappings to file
     */
    public void saveMappings() {
        String mappingsFile = AppSettings.getInstance().getCategoryTypeMappingsFile();
        if (mappingsFile != null && !mappingsFile.isEmpty()) {
            try {
                mappings.saveToFile(mappingsFile);
            } catch (IOException e) {
                showError("Error saving category mappings", e.getMessage());
            }
        }
    }

    /**
     * Adds or updates a category mapping
     */
    public void addMapping(CategoryTypeMapping mapping) {
        mappings.addMapping(mapping);
        saveMappings();
    }

    /**
     * Removes a category mapping
     */
    public void removeMapping(String category) {
        mappings.removeMapping(category);
        saveMappings();
    }

    /**
     * Gets all available mappings
     */
    public List<CategoryTypeMapping> getAllMappings() {
        return mappings.getMappings();
    }

    /**
     * Gets mapping for the specified category
     */
    public Optional<CategoryTypeMapping> getMappingByCategory(String category) {
        return mappings.getByCategory(category);
    }

    /**
     * Gets clothing types for the specified category
     */
    public List<ClothingType> getTypesForCategory(String category) {
        return mappings.getTypesForCategory(category);
    }

    /**
     * Checks if the category belongs to the specified type
     */
    public boolean isCategoryOfType(String category, ClothingType type) {
        return mappings.isCategoryOfType(category, type);
    }

    /**
     * Gets all categories belonging to the specified type
     */
    public List<String> getCategoriesOfType(ClothingType type) {
        return mappings.getCategoriesOfType(type);
    }

    /**
     * Gets all possible clothing types
     */
    public EnumSet<ClothingType> getAllClothingTypes() {
        return EnumSet.allOf(ClothingType.class);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}