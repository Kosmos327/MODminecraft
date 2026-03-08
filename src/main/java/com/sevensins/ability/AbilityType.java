package com.sevensins.ability;

/**
 * Identifies each ability available in the mod.
 * Provides helper methods for serialisation / deserialisation.
 */
public enum AbilityType {
    // WRATH (Meliodas)
    HELL_BLAZE,
    FULL_COUNTER,
    DEMON_MARK,
    DEMON_MODE,
    // PRIDE (Escanor)
    CRUEL_SUN,
    SUPERNOVA,
    THE_ONE,
    // GREED (Ban)
    SNATCH,
    FOX_HUNT,
    HUNTER_FEST,
    // SLOTH (King)
    SPIRIT_SPEAR,
    GUARDIAN,
    INCREASE,
    // LUST (Gowther)
    MIND_CONTROL,
    ILLUSION_BURST,
    MEMORY_REWRITE,
    // ENVY (Diane)
    EARTH_SMASH,
    // GLUTTONY (Merlin)
    TELEPORT,
    ARCANE_BURST,
    INFINITY_MAGIC,
    // Unimplemented stubs (reserved for future Merlin abilities)
    ENERGY_DRAIN,
    DEVOUR,
    ABYSS_SHIELD,
    INVASION,
    INFINITY,
    SUNSHINE,
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
