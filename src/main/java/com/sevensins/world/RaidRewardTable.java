package com.sevensins.world;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.registry.ModItems;
import com.sevensins.story.StoryFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Defines loot and experience rewards granted when a Night Demon Raid is completed.
 *
 * <h2>Version-1 reward baseline</h2>
 * <ul>
 *   <li>XP: {@value #RAID_XP}</li>
 *   <li>Sin Fragments: 2–{@value #RAID_FRAGMENTS_MAX}</li>
 *   <li>Magic Scroll: {@value #MAGIC_SCROLL_CHANCE_PERCENT}% chance</li>
 *   <li>Crown of Night (legendary artifact): {@value #LEGENDARY_ARTIFACT_CHANCE_PERCENT}% chance</li>
 * </ul>
 */
public final class RaidRewardTable {

    /** Vanilla XP points awarded on raid completion. */
    public static final int RAID_XP = 500;

    /** Maximum number of Sin Fragments dropped (actual count is random 2–max). */
    public static final int RAID_FRAGMENTS_MAX = 5;

    /** Percentage chance (0–100) of receiving a Magic Scroll. */
    public static final int MAGIC_SCROLL_CHANCE_PERCENT = 60;

    /** Percentage chance (0–100) of receiving the Crown of Night legendary artifact. */
    public static final int LEGENDARY_ARTIFACT_CHANCE_PERCENT = 25;

    private RaidRewardTable() {}

    /**
     * Grants the Night Demon Raid clear rewards to {@code player}.
     * Safe to call with a {@code null} player — silently skips.
     *
     * @param player the {@link ServerPlayer} who completed the raid
     */
    public static void grantRaidReward(@Nullable ServerPlayer player) {
        if (player == null) return;

        // XP reward
        player.giveExperiencePoints(RAID_XP);

        // Sin Fragments: 2 to RAID_FRAGMENTS_MAX (inclusive)
        int fragmentCount = ThreadLocalRandom.current().nextInt(2, RAID_FRAGMENTS_MAX + 1);
        player.addItem(new ItemStack(ModItems.SIN_FRAGMENT.get(), fragmentCount));

        // Magic Scroll: chance-based
        if (ThreadLocalRandom.current().nextInt(100) < MAGIC_SCROLL_CHANCE_PERCENT) {
            player.addItem(new ItemStack(ModItems.MAGIC_SCROLL.get(), 1));
        }

        // Legendary Artifact: chance-based
        if (ThreadLocalRandom.current().nextInt(100) < LEGENDARY_ARTIFACT_CHANCE_PERCENT) {
            player.addItem(new ItemStack(ModItems.CROWN_OF_NIGHT.get(), 1));
            player.sendSystemMessage(
                    Component.literal("Legendary Artifact acquired!"));
            // Set story flag
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData()
                            .addStoryFlag(StoryFlag.LEGENDARY_ARTIFACT_OBTAINED.getId()));
            // Complete the obtain_legendary_artifact quest if active
            QuestManager.completeEventQuest(player,
                    QuestRegistry.OBTAIN_LEGENDARY_ARTIFACT_ID);
        }

        SevenSinsMod.LOGGER.debug("[RaidRewardTable] Granted raid rewards to {}",
                player.getName().getString());
    }
}
