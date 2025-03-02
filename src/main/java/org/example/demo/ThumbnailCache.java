package org.example.demo;

import javafx.scene.image.Image;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для кеширования миниатюр изображений
 */
public class ThumbnailCache {
    
    private static ThumbnailCache instance;
    
    // Кеш миниатюр, где ключ - имя файла, значение - миниатюра
    private final Map<String, Image> thumbnailCache;
    
    // Размеры миниатюр по умолчанию
    private final int defaultWidth = 40;
    private final int defaultHeight = 40;
    
    private ThumbnailCache() {
        // Используем ConcurrentHashMap для потокобезопасности
        thumbnailCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Получение единственного экземпляра кеша (Singleton)
     */
    public static synchronized ThumbnailCache getInstance() {
        if (instance == null) {
            instance = new ThumbnailCache();
        }
        return instance;
    }
    
    /**
     * Получение миниатюры из кеша или загрузка ее, если она отсутствует
     * 
     * @param fileName имя файла изображения
     * @param width ширина миниатюры
     * @param height высота миниатюры
     * @return миниатюра изображения или null, если изображение не удалось загрузить
     */
    public Image getThumbnail(String fileName, int width, int height) {
        // Создаем ключ кеша, включая размеры
        String cacheKey = fileName + "_" + width + "x" + height;
        
        // Проверяем наличие в кеше
        if (thumbnailCache.containsKey(cacheKey)) {
            return thumbnailCache.get(cacheKey);
        }
        
        // Загружаем миниатюру
        Image thumbnail = loadThumbnail(fileName, width, height);
        
        // Сохраняем в кеш, если удалось загрузить
        if (thumbnail != null) {
            thumbnailCache.put(cacheKey, thumbnail);
        }
        
        return thumbnail;
    }
    
    /**
     * Получение миниатюры с размерами по умолчанию
     * 
     * @param fileName имя файла изображения
     * @return миниатюра изображения или null, если изображение не удалось загрузить
     */
    public Image getThumbnail(String fileName) {
        return getThumbnail(fileName, defaultWidth, defaultHeight);
    }
    
    /**
     * Загрузка миниатюры изображения
     * 
     * @param fileName имя файла изображения
     * @param width ширина миниатюры
     * @param height высота миниатюры
     * @return миниатюра изображения или null, если изображение не удалось загрузить
     */
    private Image loadThumbnail(String fileName, int width, int height) {
        try {
            if (fileName != null && !fileName.isEmpty()) {
                String imagesDirectory = AppSettings.getInstance().getImagesDirectory();
                if (imagesDirectory != null && !imagesDirectory.isEmpty()) {
                    File imageFile = new File(imagesDirectory, fileName);
                    if (imageFile.exists()) {
                        // Создаем миниатюру
                        return new Image(new FileInputStream(imageFile), width, height, true, true);
                    }
                }
            }
        } catch (Exception e) {
            // Если не удалось загрузить изображение, возвращаем null
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Очистка кеша
     */
    public void clearCache() {
        thumbnailCache.clear();
    }
    
    /**
     * Удаление конкретной миниатюры из кеша
     * 
     * @param fileName имя файла изображения
     */
    public void removeThumbnail(String fileName) {
        thumbnailCache.keySet().removeIf(key -> key.startsWith(fileName + "_"));
    }
    
    /**
     * Получение количества элементов в кеше
     */
    public int getCacheSize() {
        return thumbnailCache.size();
    }
}
