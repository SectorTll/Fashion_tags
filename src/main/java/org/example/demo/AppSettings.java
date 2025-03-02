package org.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppSettings {
    private static final String SETTINGS_FILE = "app_settings.json";
    private static AppSettings instance;
    private static final ObjectMapper mapper = new ObjectMapper();

    @JsonProperty("images_directory")
    private String imagesDirectory;

    @JsonProperty("wardrobe_file")
    private String wardrobeFile;

    @JsonProperty("weather_file")
    private String weatherFile;

    @JsonProperty("feelings_file")
    private String feelingsFile;

    @JsonProperty("tag_associations_file")
    private String tagAssociationsFile;

    private AppSettings() {
        this.imagesDirectory = "";
        this.wardrobeFile = "";
        this.weatherFile = "";
        this.feelingsFile = "";
        this.tagAssociationsFile = "";
    }

    public String getImagesDirectory() {
        return imagesDirectory;
    }

    public void setImagesDirectory(String imagesDirectory) {
        this.imagesDirectory = imagesDirectory;
        saveSettings();
    }

    public String getWardrobeFile() {
        return wardrobeFile;
    }

    public void setWardrobeFile(String wardrobeFile) {
        this.wardrobeFile = wardrobeFile;
        saveSettings();
    }

    public String getWeatherFile() {
        return weatherFile;
    }

    public void setWeatherFile(String weatherFile) {
        this.weatherFile = weatherFile;
        saveSettings();
    }

    public String getFeelingsFile() {
        return feelingsFile;
    }

    public void setFeelingsFile(String feelingsFile) {
        this.feelingsFile = feelingsFile;
        saveSettings();
    }


    public String getTagAssociationsFile() {
        return tagAssociationsFile;
    }

    public void setTagAssociationsFile(String tagAssociationsFile) {
        this.tagAssociationsFile = tagAssociationsFile;
        saveSettings();
    }

    // Остальные методы без изменений
    public static AppSettings getInstance() {
        if (instance == null) {
            instance = loadSettings();
        }
        return instance;
    }

    private static AppSettings loadSettings() {
        File settingsFile = getSettingsFile();
        if (settingsFile.exists()) {
            try {
                return mapper.readValue(settingsFile, AppSettings.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new AppSettings();
    }

    private void saveSettings() {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(getSettingsFile(), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getSettingsFile() {
        String userHome = System.getProperty("user.home");
        Path settingsPath = Paths.get(userHome, ".clothesml", SETTINGS_FILE);
        File settingsDir = settingsPath.getParent().toFile();
        if (!settingsDir.exists()) {
            settingsDir.mkdirs();
        }
        return settingsPath.toFile();
    }
}