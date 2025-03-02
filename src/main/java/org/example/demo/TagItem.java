package org.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий элемент тега для хранения в файле ассоциаций
 */
public class TagItem {
    
    @JsonProperty("category")
    private String category;
    
    @JsonProperty("tags")
    private List<String> tags;
    
    public TagItem() {
        this.tags = new ArrayList<>();
    }
    
    public TagItem(String category, List<String> tags) {
        this.category = category;
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }
    
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
    
    @Override
    public String toString() {
        return category.toUpperCase() + ": " + String.join(", ", tags);
    }
}
