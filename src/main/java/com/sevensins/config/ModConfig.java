package com.sevensins.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Common (server-authoritative) configuration for the Seven Deadly Sins mod.
 *
 * <p>Values are read via {@link ForgeConfigSpec} and loaded from
 * {@code config/seven_sins-common.toml} in the game directory.
 */
public class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // -------------------------------------------------------------------------
    // Sin System settings
    // -------------------------------------------------------------------------

    /** Maximum sin level a player can reach (default: 10). */
    public static final ForgeConfigSpec.IntValue SIN_LEVEL_MAX;

    /** Accumulated experience required to advance one sin level (default: 1000). */
    public static final ForgeConfigSpec.IntValue SIN_EXPERIENCE_PER_LEVEL;

    /**
     * When {@code true}, players may switch their sin alignment by using a
     * different Sin Emblem after their initial choice (default: false).
     */
    public static final ForgeConfigSpec.BooleanValue ALLOW_SIN_CHANGE;

    /**
     * When {@code true}, sin alignment and level are preserved on death even
     * without the {@code keepInventory} game rule (default: true).
     */
    public static final ForgeConfigSpec.BooleanValue KEEP_SIN_ON_DEATH;

    // -------------------------------------------------------------------------
    // Balance multiplier settings
    // -------------------------------------------------------------------------

    /** Global multiplier for all ability mana costs. */
    public static final ForgeConfigSpec.DoubleValue GLOBAL_MANA_COST_MULTIPLIER;

    /** Global multiplier for all ability cooldown durations. */
    public static final ForgeConfigSpec.DoubleValue GLOBAL_COOLDOWN_MULTIPLIER;

    /** Global multiplier for all ability damage values. */
    public static final ForgeConfigSpec.DoubleValue GLOBAL_ABILITY_DAMAGE_MULTIPLIER;

    /** Global multiplier for all boss maximum health. */
    public static final ForgeConfigSpec.DoubleValue GLOBAL_BOSS_HEALTH_MULTIPLIER;

    /** Global multiplier for all boss attack damage. */
    public static final ForgeConfigSpec.DoubleValue GLOBAL_BOSS_DAMAGE_MULTIPLIER;

    /** Global multiplier for passive ability bonuses. */
    public static final ForgeConfigSpec.DoubleValue PASSIVE_BONUS_MULTIPLIER;

    /** Global multiplier for ultimate ability bonuses. */
    public static final ForgeConfigSpec.DoubleValue ULTIMATE_BONUS_MULTIPLIER;

    /** Global multiplier for sacred treasure bonuses. */
    public static final ForgeConfigSpec.DoubleValue SACRED_TREASURE_BONUS_MULTIPLIER;

    // -------------------------------------------------------------------------
    // Compiled spec (must be last field)
    // -------------------------------------------------------------------------

    public static final ForgeConfigSpec SPEC;

    static {
        BUILDER.push("sin_system");

        SIN_LEVEL_MAX = BUILDER
                .comment("Maximum sin level achievable by a player. Range: 1–100.")
                .defineInRange("max_sin_level", 10, 1, 100);

        SIN_EXPERIENCE_PER_LEVEL = BUILDER
                .comment("Experience points required to advance one sin level. Range: 100–100 000.")
                .defineInRange("exp_per_level", 1000, 100, 100_000);

        ALLOW_SIN_CHANGE = BUILDER
                .comment("Allow players to change their sin alignment after the initial choice.")
                .define("allow_sin_change", false);

        KEEP_SIN_ON_DEATH = BUILDER
                .comment("Preserve sin alignment and level on player death.")
                .define("keep_sin_on_death", true);

        BUILDER.pop();

        BUILDER.push("balance");

        GLOBAL_MANA_COST_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to all ability mana costs. 1.0 = no change. Range: 0.1–10.0.")
                .defineInRange("global_mana_cost_multiplier", 1.0, 0.1, 10.0);

        GLOBAL_COOLDOWN_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to all ability cooldowns. 1.0 = no change. Range: 0.1–10.0.")
                .defineInRange("global_cooldown_multiplier", 1.0, 0.1, 10.0);

        GLOBAL_ABILITY_DAMAGE_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to all ability damage output. 1.0 = no change. Range: 0.1–10.0.")
                .defineInRange("global_ability_damage_multiplier", 1.0, 0.1, 10.0);

        GLOBAL_BOSS_HEALTH_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to all boss max HP. 1.0 = no change. Range: 0.1–20.0.")
                .defineInRange("global_boss_health_multiplier", 1.0, 0.1, 20.0);

        GLOBAL_BOSS_DAMAGE_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to all boss attack damage. 1.0 = no change. Range: 0.1–20.0.")
                .defineInRange("global_boss_damage_multiplier", 1.0, 0.1, 20.0);

        PASSIVE_BONUS_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to passive ability bonuses. 1.0 = no change. Range: 0.0–5.0.")
                .defineInRange("passive_bonus_multiplier", 1.0, 0.0, 5.0);

        ULTIMATE_BONUS_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to ultimate ability bonuses. 1.0 = no change. Range: 0.0–5.0.")
                .defineInRange("ultimate_bonus_multiplier", 1.0, 0.0, 5.0);

        SACRED_TREASURE_BONUS_MULTIPLIER = BUILDER
                .comment("Global multiplier applied to sacred treasure bonuses. 1.0 = no change. Range: 0.0–5.0.")
                .defineInRange("sacred_treasure_bonus_multiplier", 1.0, 0.0, 5.0);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

