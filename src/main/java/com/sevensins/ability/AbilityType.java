package com.sevensins.ability;

/**
 * Identifies each ability available in the mod.
 * Provides helper methods for serialisation / deserialisation.
 */
public enum AbilityType {
    FULL_COUNTER,
    DEMON_MODE,
    EARTH_SMASH,
    SPIRIT_SPEAR,
    SNATCH,
    INVASION,
    INFINITY,
    SUNSHINE,
    HELL_BLAZE,
    NONE;

    /** Returns the lower-case name used when persisting to NBT or JSON. */
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    /**
     * Looks up an {@link AbilityType} by its serialised name (case-insensitive).
     *
     * @param value serialised name, or {@code null}
     * @return matching type, or {@link #NONE} if not found
     */
    public static AbilityType fromName(String value) {
        if (value == null) {
            return NONE;
        }
        for (AbilityType type : values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return NONE;
    }
}
