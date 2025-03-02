package org.example.demo;

import java.util.List;





import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OutfitDetails {
    @JsonProperty("is_valid_outfit")  // Связываем с JSON полем is_valid_outfit
    private boolean isValidOutfit;

    @JsonProperty("error_message")    // Связываем с JSON полем error_message
    private String errorMessage;

    @JsonProperty("outfit")           // Это поле совпадает, но добавим для единообразия
    private List<OutfitItem> outfit;

    // Геттеры и сеттеры
    public boolean isValidOutfit() {
        return isValidOutfit;
    }

    public void setValidOutfit(boolean isValidOutfit) {
        this.isValidOutfit = isValidOutfit;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<OutfitItem> getOutfit() {
        return outfit;
    }

    public void setOutfit(List<OutfitItem> outfit) {
        this.outfit = outfit;
    }
}

