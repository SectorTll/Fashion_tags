package org.example.demo.wardrobe;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class WardrobeItem {
    @JsonProperty("category")
    private String category;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("file_name")
    private String fileName;

    // Конструкторы
    public WardrobeItem() {}

    public WardrobeItem(String category, List<String> tags, String fileName) {
        this.category = category;
        this.tags = tags;
        this.fileName = fileName;
    }

    // Геттеры и сеттеры
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "WardrobeItem{" +
                "category='" + category + '\'' +
                ", tags=" + tags +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}