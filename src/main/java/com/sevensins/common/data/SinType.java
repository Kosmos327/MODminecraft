package com.sevensins.common.data;

import java.util.Optional;

/**
 * Represents each of the Seven Deadly Sins.
 * Each sin has an id used for registry names, a display color, and translation keys.
 */
public enum SinType {

    WRATH("wrath", 0xFF4500),
    GREED("greed", 0xFFD700),
    SLOTH("sloth", 0x8B8682),
    PRIDE("pride", 0xE8E8E8),
    LUST("lust", 0xFF1493),
    ENVY("envy", 0x228B22),
    GLUTTONY("gluttony", 0xFF8C00);

    private static final String MOD_ID = "seven_sins";

    private final String id;
    private final int color;

    SinType(String id, int color) {
        this.id = id;
        this.color = color;
    }

    /** Registry-safe lowercase id. */
    public String getId() {
        return id;
    }

    /** ARGB color integer used for HUD and particles. */
    public int getColor() {
        return color;
    }

    /** Translation key for the sin's display name. */
    public String getTranslationKey() {
        return "sin." + MOD_ID + "." + id;
    }

    /** Translation key for the sin's lore description. */
    public String getDescriptionKey() {
        return "sin." + MOD_ID + "." + id + ".description";
    }

    /**
     * Looks up a SinType by its string id.
     *
     * @param id the lowercase id (e.g. "wrath")
     * @return an Optional containing the matching SinType, or empty if not found
     */
    public static Optional<SinType> fromId(String id) {
        for (SinType sin : values()) {
            if (sin.id.equals(id)) {
                return Optional.of(sin);
            }
        }
        return Optional.empty();
    }
}
