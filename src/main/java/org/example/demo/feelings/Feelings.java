package org.example.demo.feelings;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class for storing a collection of style sets
 */
public class Feelings {

    @JsonProperty("feelings")
    private List<Feeling> feelings;

    private static final ObjectMapper mapper = new ObjectMapper();

    // Available clothing styles
    public static final List<String> AVAILABLE_STYLES = List.of(
            "casual", "formal", "sport", "elegant", "business", "trendy", "chic", "classic", "streetwear", "vintage",
            "outdoor", "indoor", "office", "party", "travel", "home", "date", "beach", "gym", "walking", "hiking",
            "meeting", "work"
    );

    // Constructors
    public Feelings() {
        this.feelings = new ArrayList<>();
    }

    // Methods for working with style sets collection
    public void addFeeling(Feeling feeling) {
        // Check for existing set with this name
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

    // Get style set by name
    public Optional<Feeling> getByName(String name) {
        return feelings.stream()
                .filter(feeling -> feeling.getName().equals(name))
                .findFirst();
    }

    // Save and load
    public void saveToFile(String filepath) throws IOException {
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filepath), this);
    }

    public static Feelings loadFromFile(String filepath) throws IOException {
        return mapper.readValue(new File(filepath), Feelings.class);
    }
}