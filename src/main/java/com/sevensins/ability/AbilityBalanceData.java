package com.sevensins.ability;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stores the base balance tuning values for a single ability.
 *
 * <p>The registry maps each {@link AbilityType} to its {@link AbilityBalanceData}
 * so future re-balancing only requires changing values here rather than inside
 * every concrete ability class.</p>
 *
 * <p>When no entry exists for a given ability, {@link #get(AbilityType)} returns
 * a safe default so callers never need to null-check.</p>
 */
public final class AbilityBalanceData {

    // -------------------------------------------------------------------------
    // Safe default (used as fallback when ability has no registered entry)
    // -------------------------------------------------------------------------

    private static final AbilityBalanceData DEFAULT = new AbilityBalanceData(10, 60, 5.0f);

    // -------------------------------------------------------------------------
    // Registry
    // -------------------------------------------------------------------------

    private static final Map<AbilityType, AbilityBalanceData> REGISTRY =
            new EnumMap<>(AbilityType.class);

    static {
        // Wrath (Meliodas)
        REGISTRY.put(AbilityType.HELL_BLAZE,      new AbilityBalanceData(20, 100, 8.0f));
        REGISTRY.put(AbilityType.FULL_COUNTER,    new AbilityBalanceData(20, 200, 0.0f));
        REGISTRY.put(AbilityType.DEMON_MARK,      new AbilityBalanceData(30, 300, 0.0f));
        REGISTRY.put(AbilityType.DEMON_MODE,      new AbilityBalanceData(50, 600, 0.0f));
        // Pride (Escanor)
        REGISTRY.put(AbilityType.CRUEL_SUN,       new AbilityBalanceData(25, 120, 10.0f));
        REGISTRY.put(AbilityType.SUPERNOVA,       new AbilityBalanceData(40, 400, 20.0f));
        REGISTRY.put(AbilityType.THE_ONE,         new AbilityBalanceData(60, 1200, 0.0f));
        // Greed (Ban)
        REGISTRY.put(AbilityType.SNATCH,          new AbilityBalanceData(15, 80, 5.0f));
        REGISTRY.put(AbilityType.FOX_HUNT,        new AbilityBalanceData(25, 150, 12.0f));
        REGISTRY.put(AbilityType.HUNTER_FEST,     new AbilityBalanceData(35, 300, 0.0f));
        // Sloth (King)
        REGISTRY.put(AbilityType.SPIRIT_SPEAR,    new AbilityBalanceData(20, 100, 9.0f));
        REGISTRY.put(AbilityType.GUARDIAN,        new AbilityBalanceData(30, 200, 0.0f));
        REGISTRY.put(AbilityType.INCREASE,        new AbilityBalanceData(25, 150, 0.0f));
        // Lust (Gowther)
        REGISTRY.put(AbilityType.MIND_CONTROL,    new AbilityBalanceData(30, 200, 0.0f));
        REGISTRY.put(AbilityType.ILLUSION_BURST,  new AbilityBalanceData(25, 120, 8.0f));
        REGISTRY.put(AbilityType.MEMORY_REWRITE,  new AbilityBalanceData(40, 400, 0.0f));
        // Envy (Diane)
        REGISTRY.put(AbilityType.TELEPORT,        new AbilityBalanceData(15, 60, 0.0f));
        REGISTRY.put(AbilityType.ARCANE_BURST,    new AbilityBalanceData(30, 180, 12.0f));
        REGISTRY.put(AbilityType.INFINITY_MAGIC,  new AbilityBalanceData(50, 500, 0.0f));
        REGISTRY.put(AbilityType.EARTH_SMASH,     new AbilityBalanceData(25, 160, 10.0f));
        // Gluttony (Merlin)
        REGISTRY.put(AbilityType.ENERGY_DRAIN,    new AbilityBalanceData(20, 100, 7.0f));
        REGISTRY.put(AbilityType.DEVOUR,          new AbilityBalanceData(35, 250, 15.0f));
        REGISTRY.put(AbilityType.ABYSS_SHIELD,    new AbilityBalanceData(40, 300, 0.0f));
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final int baseMana;
    private final int baseCooldown;
    private final float baseDamage;

    public AbilityBalanceData(int baseMana, int baseCooldown, float baseDamage) {
        this.baseMana = baseMana;
        this.baseCooldown = baseCooldown;
        this.baseDamage = baseDamage;
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Base mana cost before any modifiers are applied. */
    public int getBaseMana() {
        return baseMana;
    }

    /** Base cooldown in ticks before any modifiers are applied. */
    public int getBaseCooldown() {
        return baseCooldown;
    }

    /** Base ability damage before any modifiers are applied. */
    public float getBaseDamage() {
        return baseDamage;
    }

    // -------------------------------------------------------------------------
    // Registry accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the balance data for the given ability type, falling back to a
     * safe default when no entry is registered.
     *
     * @param type the ability to look up
     * @return registered or default {@link AbilityBalanceData}; never {@code null}
     */
    public static AbilityBalanceData get(AbilityType type) {
        if (type == null) {
            return DEFAULT;
        }
        return REGISTRY.getOrDefault(type, DEFAULT);
    }

    /**
     * Registers or replaces the balance data for a specific ability type.
     * Intended for use by addon modules or test setups.
     *
     * @param type the ability to register
     * @param data the balance data to associate with it
     */
    public static void register(AbilityType type, AbilityBalanceData data) {
        if (type != null && data != null) {
            REGISTRY.put(type, data);
        }
    }
}
