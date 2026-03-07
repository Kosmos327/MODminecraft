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

    /** Vanilla XP points awarded to the killer of the Demon King. */
    public static final int DEMON_KING_XP = 1500;

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
     * Grants the Demon King kill reward to {@code player}.
     * Awards {@value #DEMON_KING_XP} XP.
     * Safe to call with a {@code null} player — the reward is silently skipped.
     *
     * @param player the {@link ServerPlayer} who killed the Demon King
     */
    public static void onDemonKingDeath(@Nullable ServerPlayer player) {
        if (player == null) return;
        player.giveExperiencePoints(DEMON_KING_XP);
    }
}
