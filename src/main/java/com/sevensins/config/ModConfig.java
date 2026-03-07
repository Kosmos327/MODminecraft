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

        SPEC = BUILDER.build();
    }
}
