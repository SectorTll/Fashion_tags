package org.example.demo.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий связь между категорией одежды и типами
 */
public class CategoryTypeMapping {

    @JsonProperty("category")
    private String category;

    @JsonProperty("types")
    private List<ClothingType> types;

    public CategoryTypeMapping() {
        this.types = new ArrayList<>();
    }

    public CategoryTypeMapping(String category, List<ClothingType> types) {
        this.category = category;
        this.types = types != null ? new ArrayList<>(types) : new ArrayList<>();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<ClothingType> getTypes() {
        return types;
    }

    public void setTypes(List<ClothingType> types) {
        this.types = types;
    }

    public void addType(ClothingType type) {
        if (!types.contains(type)) {
            types.add(type);
        }
    }

    public void removeType(ClothingType type) {
        types.remove(type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(category);
        sb.append(": ");
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(types.get(i).getDisplayName());
        }
        return sb.toString();
    }
}