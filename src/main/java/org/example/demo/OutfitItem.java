package org.example.demo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutfitItem {
    @JsonProperty("category")
    private String category;

    @JsonProperty("tags")
    private List<String> tags;

    // Геттеры и сеттеры остаются без изменений
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
}
