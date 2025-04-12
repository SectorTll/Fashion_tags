package org.example.demo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс для работы с хранилищем ассоциаций тегов с изображениями
 */
public class TagAssociationsStorage {

    private static final String DEFAULT_FILENAME = "tag_associations.json";
    private static TagAssociationsStorage instance;

    @JsonProperty("tag_associations")
    private List<TagAssociation> tagAssociations;

    // Исключаем поле из сериализации и десериализации
    @JsonIgnore
    private String storageFilePath;

    private TagAssociationsStorage() {
        this.tagAssociations = new ArrayList<>();

        // Проверяем, указан ли путь в настройках
        String configPath = AppSettings.getInstance().getTagAssociationsFile();
        if (configPath != null && !configPath.isEmpty()) {
            this.storageFilePath = configPath;
        } else {
            this.storageFilePath = getDefaultStoragePath();
        }
    }

    public static TagAssociationsStorage getInstance() {
        if (instance == null) {
            instance = new TagAssociationsStorage();
            instance.loadFromFile();
        } else {
            // Проверяем, не изменился ли путь в настройках
            String configPath = AppSettings.getInstance().getTagAssociationsFile();
            if (configPath != null && !configPath.isEmpty() &&
                    !instance.storageFilePath.equals(configPath)) {
                instance.storageFilePath = configPath;
                instance.loadFromFile();
            }
        }
        return instance;
    }

    /**
     * Загружает ассоциации из файла
     */
    public void loadFromFile() {
        File file = new File(storageFilePath);
        if (file.exists()) {
            try {
                // Создаем новый экземпляр маппера для каждой операции чтения
                ObjectMapper fileMapper = new ObjectMapper();

                // Вместо десериализации в объект класса, загружаем в простую структуру
                TagAssociationsWrapper wrapper = fileMapper.readValue(file, TagAssociationsWrapper.class);

                if (wrapper != null && wrapper.tagAssociations != null) {
                    this.tagAssociations = wrapper.tagAssociations;
                } else {
                    System.err.println("Field tag_associations not found in file or it is empty");
                    this.tagAssociations = new ArrayList<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error loading association file: " + e.getMessage());
                // В случае ошибки начинаем с пустого списка
                this.tagAssociations = new ArrayList<>();
            }
        } else {
            System.out.println("Association file not found, creating a new one: " + storageFilePath);
            this.tagAssociations = new ArrayList<>();
        }
    }

    /**
     * Вспомогательный класс для десериализации JSON
     */
    private static class TagAssociationsWrapper {
        @JsonProperty("tag_associations")
        public List<TagAssociation> tagAssociations;
    }

    /**
     * Сохраняет ассоциации в файл
     */
    public void saveToFile() throws IOException {
        File file = new File(storageFilePath);

        // Убедимся, что директория существует
        File directory = file.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Создаем новый экземпляр маппера для каждой операции записи
        ObjectMapper fileMapper = new ObjectMapper();

        // Вместо сериализации всего объекта, создаем простую обертку
        TagAssociationsWrapper wrapper = new TagAssociationsWrapper();
        wrapper.tagAssociations = this.tagAssociations;

        fileMapper.writerWithDefaultPrettyPrinter().writeValue(file, wrapper);
    }

    /**
     * Устанавливает путь к файлу хранилища
     */
    public void setStorageFilePath(String path) {
        this.storageFilePath = path;
        loadFromFile();
    }

    /**
     * Возвращает текущий путь к файлу хранилища
     */
    @JsonIgnore
    public String getStorageFilePath() {
        return storageFilePath;
    }

    /**
     * Возвращает путь к файлу хранилища по умолчанию
     */
    private String getDefaultStoragePath() {
        String userHome = System.getProperty("user.home");
        Path path = Paths.get(userHome, ".clothesml", DEFAULT_FILENAME);
        return path.toString();
    }

    /**
     * Получает ассоциацию для конкретного изображения
     */
    public Optional<TagAssociation> getAssociationForImage(String imageFileName) {
        return tagAssociations.stream()
                .filter(assoc -> assoc.getImageFileName().equals(imageFileName))
                .findFirst();
    }

    /**
     * Добавляет или обновляет ассоциацию для изображения
     */
    public void saveAssociation(TagAssociation association) {
        // Удаляем существующую ассоциацию при наличии
        tagAssociations.removeIf(assoc ->
                assoc.getImageFileName().equals(association.getImageFileName()));

        // Добавляем новую ассоциацию
        tagAssociations.add(association);

        // Сохраняем изменения в файл
        try {
            saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удаляет ассоциацию для изображения
     */
    public void removeAssociation(String imageFileName) {
        tagAssociations.removeIf(assoc ->
                assoc.getImageFileName().equals(imageFileName));

        // Сохраняем изменения в файл
        try {
            saveToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Возвращает все ассоциации
     */
    @JsonIgnore
    public List<TagAssociation> getAllAssociations() {
        return new ArrayList<>(tagAssociations);
    }
}