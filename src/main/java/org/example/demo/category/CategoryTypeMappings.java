package org.example.demo.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Класс для хранения коллекции маппингов категорий на типы
 */
public class CategoryTypeMappings {

    @JsonProperty("mappings")
    private List<CategoryTypeMapping> mappings;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Конструкторы
    public CategoryTypeMappings() {
        this.mappings = new ArrayList<>();
    }

    // Методы для работы с коллекцией маппингов
    public void addMapping(CategoryTypeMapping mapping) {
        // Проверяем существование маппинга с такой категорией
        Optional<CategoryTypeMapping> existingMapping = getByCategory(mapping.getCategory());
        if (existingMapping.isPresent()) {
            mappings.remove(existingMapping.get());
        }
        mappings.add(mapping);
    }

    public void removeMapping(String category) {
        mappings.removeIf(mapping -> mapping.getCategory().equals(category));
    }

    public List<CategoryTypeMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<CategoryTypeMapping> mappings) {
        this.mappings = mappings;
    }

    // Получение маппинга по категории
    public Optional<CategoryTypeMapping> getByCategory(String category) {
        return mappings.stream()
                .filter(mapping -> mapping.getCategory().equals(category))
                .findFirst();
    }

    /**
     * Получает типы одежды для указанной категории
     *
     * @param category категория одежды
     * @return список типов или пустой список, если категория не найдена
     */
    public List<ClothingType> getTypesForCategory(String category) {
        Optional<CategoryTypeMapping> mapping = getByCategory(category);
        return mapping.map(CategoryTypeMapping::getTypes).orElse(new ArrayList<>());
    }

    /**
     * Проверяет, относится ли категория к указанному типу
     *
     * @param category категория одежды
     * @param type тип одежды
     * @return true, если категория относится к указанному типу
     */
    public boolean isCategoryOfType(String category, ClothingType type) {
        Optional<CategoryTypeMapping> mapping = getByCategory(category);
        return mapping.isPresent() && mapping.get().getTypes().contains(type);
    }

    /**
     * Получает все категории, относящиеся к указанному типу
     *
     * @param type тип одежды
     * @return список категорий, относящихся к указанному типу
     */
    public List<String> getCategoriesOfType(ClothingType type) {
        List<String> categories = new ArrayList<>();
        for (CategoryTypeMapping mapping : mappings) {
            if (mapping.getTypes().contains(type)) {
                categories.add(mapping.getCategory());
            }
        }
        return categories;
    }

    // Сохранение и загрузка
    public void saveToFile(String filepath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filepath), this);
    }

    public static CategoryTypeMappings loadFromFile(String filepath) throws IOException {
        return mapper.readValue(new File(filepath), CategoryTypeMappings.class);
    }

    /**
     * Создает маппинги по умолчанию для типовых категорий одежды
     */
    public static CategoryTypeMappings createDefaultMappings() {
        CategoryTypeMappings mappings = new CategoryTypeMappings();




        mappings.addMapping(new CategoryTypeMapping("dress", Arrays.asList(ClothingType.TORSO, ClothingType.HIPS)));

        return mappings;
    }
}