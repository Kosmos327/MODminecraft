package com.sevensins.world;

import com.sevensins.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Defines loot and experience rewards granted when a dungeon is cleared.
 *
 * <h2>Version-1 reward baseline (Demon Cave)</h2>
 * <ul>
 *   <li>XP: {@value #DEMON_CAVE_XP}</li>
 *   <li>Sin Fragments: 1–{@value #DEMON_CAVE_FRAGMENTS_MAX}</li>
 *   <li>Magic Scroll: {@value #MAGIC_SCROLL_CHANCE_PERCENT}% chance</li>
 * </ul>
 */
public final class DungeonRewardTable {

    /** Vanilla XP points awarded on Demon Cave clear. */
    public static final int DEMON_CAVE_XP = 150;

    /** Maximum number of Sin Fragments dropped (actual count is random 1–max). */
    public static final int DEMON_CAVE_FRAGMENTS_MAX = 3;

    /** Percentage chance (0–100) of receiving a Magic Scroll. */
    public static final int MAGIC_SCROLL_CHANCE_PERCENT = 30;

    private DungeonRewardTable() {}

    /**
     * Grants the Demon Cave clear rewards to {@code player}.
     * Safe to call with a {@code null} player — silently skips.
     *
     * @param player the {@link ServerPlayer} who cleared the dungeon
     */
    public static void grantDemonCaveReward(@Nullable ServerPlayer player) {
        if (player == null) return;

        // XP reward
        player.giveExperiencePoints(DEMON_CAVE_XP);

        // Sin Fragments: 1 to DEMON_CAVE_FRAGMENTS_MAX
        int fragmentCount = 1 + ThreadLocalRandom.current().nextInt(DEMON_CAVE_FRAGMENTS_MAX);
        player.addItem(new ItemStack(ModItems.SIN_FRAGMENT.get(), fragmentCount));

        // Magic Scroll: chance-based
        if (ThreadLocalRandom.current().nextInt(100) < MAGIC_SCROLL_CHANCE_PERCENT) {
            player.addItem(new ItemStack(ModItems.MAGIC_SCROLL.get(), 1));
        }
    }
}
