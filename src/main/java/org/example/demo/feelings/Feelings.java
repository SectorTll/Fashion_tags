package org.example.demo.feelings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Класс для хранения коллекции наборов стилей
 */
public class Feelings {
    
    @JsonProperty("feelings")
    private List<Feeling> feelings;
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    // Доступные стили одежды
    public static final List<String> AVAILABLE_STYLES = List.of(
        "casual", "formal", "sport", "elegant", "business", "trendy", "chic", "classic", "streetwear", "vintage",
        "outdoor", "indoor", "office", "party", "travel", "home", "date", "beach", "gym", "walking", "hiking",
        "meeting", "work"
    );
    
    // Конструкторы
    public Feelings() {
        this.feelings = new ArrayList<>();
    }
    
    // Методы для работы с коллекцией наборов стилей
    public void addFeeling(Feeling feeling) {
        // Проверяем существование набора с таким именем
        Optional<Feeling> existingFeeling = getByName(feeling.getName());
        if (existingFeeling.isPresent()) {
            feelings.remove(existingFeeling.get());
        }
        feelings.add(feeling);
    }
    
    public void removeFeeling(String name) {
        feelings.removeIf(feeling -> feeling.getName().equals(name));
    }
    
    public List<Feeling> getFeelings() {
        return feelings;
    }
    
    public void setFeelings(List<Feeling> feelings) {
        this.feelings = feelings;
    }
    
    // Получение набора стилей по имени
    public Optional<Feeling> getByName(String name) {
        return feelings.stream()
                .filter(feeling -> feeling.getName().equals(name))
                .findFirst();
    }
    
    // Сохранение и загрузка
    public void saveToFile(String filepath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(filepath), this);
    }
    
    public static Feelings loadFromFile(String filepath) throws IOException {
        return mapper.readValue(new File(filepath), Feelings.class);
    }
}