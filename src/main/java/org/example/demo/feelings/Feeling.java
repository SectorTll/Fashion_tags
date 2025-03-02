package org.example.demo.feelings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий набор стилей одежды
 */
public class Feeling {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("styles")
    private List<String> styles;
    
    // Конструкторы
    public Feeling() {
        this.styles = new ArrayList<>();
    }
    
    public Feeling(String name, List<String> styles) {
        this.name = name;
        this.styles = styles != null ? new ArrayList<>(styles) : new ArrayList<>();
    }
    
    // Геттеры и сеттеры
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<String> getStyles() {
        return styles;
    }
    
    public void setStyles(List<String> styles) {
        this.styles = styles;
    }
    
    // Добавление стиля в набор
    public void addStyle(String style) {
        if (!styles.contains(style)) {
            styles.add(style);
        }
    }
    
    // Удаление стиля из набора
    public void removeStyle(String style) {
        styles.remove(style);
    }
    
    @Override
    public String toString() {
        return name + ": " + String.join(", ", styles);
    }
}