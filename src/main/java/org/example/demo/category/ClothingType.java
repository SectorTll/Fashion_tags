package org.example.demo.category;

/**
 * Enumeration of clothing types
 */
public enum ClothingType {
    HEAD("Head"),
    TORSO("Torso"),
    HIPS("Hips"),
    LEGS("Legs"),
    ACCESSORY("Accessory");

    private final String name;


    ClothingType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }



    /**
     * Returns the display name, considering localization
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Get type by name (case insensitive)
     */
    public static ClothingType fromString(String text) {
        for (ClothingType type : ClothingType.values()) {
            if (type.name().equalsIgnoreCase(text) ||
                    type.getName().equalsIgnoreCase(text)
            ) {
                return type;
            }
        }
        return null;
    }
}