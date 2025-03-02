package org.example.demo.wardrobe;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Wardrobe {
    @JsonProperty("items")
    private List<WardrobeItem> items;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Конструкторы
    public Wardrobe() {
        this.items = new ArrayList<>();
    }

    // Методы для работы с гардеробом
    public void addItem(WardrobeItem item) {
        items.add(item);
    }

    public void removeItem(WardrobeItem item) {
        items.remove(item);
    }

    public List<WardrobeItem> getItems() {
        return items;
    }

    public void setItems(List<WardrobeItem> items) {
        this.items = items;
    }

    // Методы для фильтрации и поиска
    public List<WardrobeItem> getItemsByCategory(String category) {
        return items.stream()
                .filter(item -> item.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public List<WardrobeItem> getItemsByTag(String tag) {
        return items.stream()
                .filter(item -> item.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    // Получить все уникальные категории
    public List<String> getAllCategories() {
        return items.stream()
                .map(WardrobeItem::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    // Получить все уникальные теги
    public List<String> getAllTags() {
        return items.stream()
                .flatMap(item -> item.getTags().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    // Сохранение и загрузка
    public void saveToFile(String filepath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(filepath), this);
    }

    public static Wardrobe loadFromFile(String filepath) throws IOException {
        return mapper.readValue(new File(filepath), Wardrobe.class);
    }

    // Статистика гардероба
    public Map<String, Long> getCategoryStatistics() {
        return items.stream()
                .collect(Collectors.groupingBy(
                        WardrobeItem::getCategory,
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getTagStatistics() {
        return items.stream()
                .flatMap(item -> item.getTags().stream())
                .collect(Collectors.groupingBy(
                        tag -> tag,
                        Collectors.counting()
                ));
    }

    @Override
    public String toString() {
        return "Wardrobe{" +
                "items=" + items +
                '}';
    }
}