package org.example.demo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

import org.example.demo.feelings.Feeling;
import org.example.demo.weather.Weather;

/**
 * Класс, представляющий связь между изображением и тегами (оригинальными и эталонными)
 */
public class TagAssociation {

    @JsonProperty("image_file_name")
    private String imageFileName;

    @JsonProperty("original")
    private List<TagItem> originalTagItems;

    @JsonProperty("reference")
    private List<TagItem> referenceTagItems;

    @JsonProperty("weather")
    private Weather weather;

    @JsonProperty("feeling")
    private Feeling feeling;

    public TagAssociation() {
        this.originalTagItems = new ArrayList<>();
        this.referenceTagItems = new ArrayList<>();
        this.feeling = new Feeling();
        this.weather = new Weather();

    }

    public TagAssociation(String imageFileName) {
        this.imageFileName = imageFileName;
        this.originalTagItems = new ArrayList<>();
        this.referenceTagItems = new ArrayList<>();
        this.feeling = new Feeling();
        this.weather = new Weather();
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public List<TagItem> getOriginalTagItems() {
        return originalTagItems;
    }

    public void setOriginalTagItems(List<TagItem> originalTagItems) {
        this.originalTagItems = originalTagItems;
    }

    public List<TagItem> getReferenceTagItems() {
        return referenceTagItems;
    }

    public void setReferenceTagItems(List<TagItem> referenceTagItems) {
        this.referenceTagItems = referenceTagItems;
    }

    /**
     * Добавляет тег элемента гардероба в список эталонных тегов
     */
    public void addReferenceTagItem(TagItem tagItem) {
        if (!containsReferenceTagItem(tagItem)) {
            referenceTagItems.add(tagItem);
        }
    }

    /**
     * Удаляет тег элемента гардероба из списка эталонных тегов
     */
    public void removeReferenceTagItem(TagItem tagItem) {
        referenceTagItems.removeIf(item ->
                item.getCategory().equals(tagItem.getCategory()) &&
                        item.getTags().containsAll(tagItem.getTags()) &&
                        tagItem.getTags().containsAll(item.getTags()));
    }

    /**
     * Проверяет, содержит ли список уже такой эталонный тег
     */
    public boolean containsReferenceTagItem(TagItem tagItem) {
        return referenceTagItems.stream().anyMatch(item ->
                item.getCategory().equals(tagItem.getCategory()) &&
                        item.getTags().containsAll(tagItem.getTags()) &&
                        tagItem.getTags().containsAll(item.getTags()));
    }

    /**
     * Добавляет оригинальный тег изображения
     */
    public void addOriginalTagItem(TagItem tagItem) {
        if (!containsOriginalTagItem(tagItem)) {
            originalTagItems.add(tagItem);
        }
    }

    /**
     * Проверяет, содержит ли список уже такой оригинальный тег
     */
    public boolean containsOriginalTagItem(TagItem tagItem) {
        return originalTagItems.stream().anyMatch(item ->
                item.getCategory().equals(tagItem.getCategory()) &&
                        item.getTags().containsAll(tagItem.getTags()) &&
                        tagItem.getTags().containsAll(item.getTags()));
    }

    public Weather getWeather() {
        return weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public Feeling getFeeling() {
        return feeling;
    }

    public void setFeeling(Feeling feeling) {
        this.feeling = feeling;
    }
}