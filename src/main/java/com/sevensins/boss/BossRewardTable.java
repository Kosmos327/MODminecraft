package com.sevensins.boss;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

/**
 * Defines loot and experience rewards granted when a boss is defeated.
 *
 * <p>Version 1 grants only raw Minecraft experience to the player who
 * dealt the final blow (or the nearest attacking player when the killer
 * cannot be resolved).</p>
 */
public final class BossRewardTable {

    /** Vanilla XP points awarded to the killer of the Red Demon. */
    public static final int RED_DEMON_XP = 250;

    /** Vanilla XP points awarded to the killer of the Gray Demon. */
    public static final int GRAY_DEMON_XP = 350;

    /** Vanilla XP points awarded to the killer of the Demon Commander. */
    public static final int DEMON_COMMANDER_XP = 500;

    private BossRewardTable() {}

    /**
     * Grants the Red Demon kill reward to {@code player}.
     * Safe to call with a {@code null} player — the reward is silently skipped.
     *
     * @param player the {@link ServerPlayer} who killed the boss
     */
    public static void onBossDeath(@Nullable ServerPlayer player) {
        if (player == null) return;
        player.giveExperiencePoints(RED_DEMON_XP);
    }

    /**
     * Grants the Gray Demon kill reward to {@code player}.
     * Safe to call with a {@code null} player — the reward is silently skipped.
     *
     * @param player the {@link ServerPlayer} who killed the boss
     */
    public static void onGrayDemonDeath(@Nullable ServerPlayer player) {
        if (player == null) return;
        player.giveExperiencePoints(GRAY_DEMON_XP);
    }

    /**
     * Grants the Demon Commander kill reward to {@code player}.
     * Safe to call with a {@code null} player — the reward is silently skipped.
     *
     * @param player the {@link ServerPlayer} who killed the boss
     */
    public static void onDemonCommanderDeath(@Nullable ServerPlayer player) {
        if (player == null) return;
        player.giveExperiencePoints(DEMON_COMMANDER_XP);
    }
}
