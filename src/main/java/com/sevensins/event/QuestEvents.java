package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossRewardTable;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.entity.GrayDemonEntity;
import com.sevensins.entity.RedDemonEntity;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side event handler that tracks {@link com.sevensins.quest.QuestType#KILL}
 * quest progress.
 *
 * <h2>Kill counting rules</h2>
 * <ul>
 *   <li>The dying entity must be a {@link Monster} (hostile mob) — passive
 *       animals and players are excluded.</li>
 *   <li>The source of death must be a {@link ServerPlayer}.</li>
 *   <li>That player must have chosen a character (not {@link CharacterType#NONE}).</li>
 *   <li>The player must have an active KILL quest.</li>
 * </ul>
 *
 * <p>Boss kills ({@link RedDemonEntity}) are handled first via
 * {@link #handleRedDemonBossLogic} and do not count toward generic kill quests.</p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestEvents {

    private QuestEvents() {}

    /**
     * Main handler — called for every living entity death.
     *
     * <p>Boss entities are handled first and return early to avoid also counting
     * toward generic kill quests.</p>
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Killer must be a server-side player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        // --- Boss-specific handling ---
        if (event.getEntity() instanceof RedDemonEntity redDemon) {
            handleRedDemonBossLogic(redDemon, killer);
            return; // boss kill does not also count toward generic kill quests
        }

        if (event.getEntity() instanceof GrayDemonEntity grayDemon) {
            handleGrayDemonBossLogic(grayDemon, killer);
            return; // boss kill does not also count toward generic kill quests
        }

        // --- Generic monster kill handling ---
        if (!(event.getEntity() instanceof Monster monster)) return;

        // Player must have a character selected
        ModCapabilities.get(killer).ifPresent(cap -> {
            if (cap.getData().getSelectedCharacter() == CharacterType.NONE) return;

            // For the chapter-2 quest, only count powerful mobs (max health >= 20)
            String activeId = cap.getData().getQuestData().getActiveQuestId();
            if (QuestRegistry.FIRST_DEMON_HUNT_ID.equals(activeId)) {
                if (monster.getMaxHealth() < 20.0f) return;
            }

            QuestManager.incrementKillProgress(killer);
        });
    }

    // -------------------------------------------------------------------------
    // Boss death logic
    // -------------------------------------------------------------------------

    /**
     * Handles all side-effects of the Red Demon being killed by a player:
     * <ol>
     *   <li>Unregisters the boss from {@link BossManager}.</li>
     *   <li>Grants the XP reward via {@link BossRewardTable}.</li>
     *   <li>Completes the {@value QuestRegistry#SLAY_RED_DEMON_ID} quest if active.</li>
     *   <li>Broadcasts a server-wide defeat message.</li>
     * </ol>
     */
    private static void handleRedDemonBossLogic(RedDemonEntity redDemon, ServerPlayer killer) {
        // Unregister from boss tracker
        BossManager.getInstance().unregisterBoss(redDemon.getUUID());

        // Grant XP reward to the killer
        BossRewardTable.onBossDeath(killer);

        // Complete the slay_red_demon quest if the killer has it active
        ModCapabilities.get(killer).ifPresent(cap -> {
            if (cap.getData().getSelectedCharacter() == CharacterType.NONE) return;
            String activeId = cap.getData().getQuestData().getActiveQuestId();
            if (QuestRegistry.SLAY_RED_DEMON_ID.equals(activeId)) {
                QuestManager.completeBossKillQuest(killer, QuestRegistry.SLAY_RED_DEMON_ID);
            }
        });

        // Server-wide broadcast
        if (killer.getServer() != null) {
            killer.getServer().getPlayerList()
                    .broadcastSystemMessage(
                            Component.literal("The Red Demon has been defeated."), false);
        }
    }

    /**
     * Handles all side-effects of the Gray Demon being killed by a player:
     * <ol>
     *   <li>Unregisters the boss from {@link BossManager}.</li>
     *   <li>Grants the XP reward via {@link BossRewardTable#onGrayDemonDeath}.</li>
     *   <li>Completes the {@value QuestRegistry#SLAY_GRAY_DEMON_ID} quest if active.</li>
     *   <li>Broadcasts a server-wide defeat message.</li>
     * </ol>
     */
    private static void handleGrayDemonBossLogic(GrayDemonEntity grayDemon, ServerPlayer killer) {
        // Unregister from boss tracker
        BossManager.getInstance().unregisterBoss(grayDemon.getUUID());

        // Grant XP reward to the killer
        BossRewardTable.onGrayDemonDeath(killer);

        // Complete the slay_gray_demon quest if the killer has it active
        ModCapabilities.get(killer).ifPresent(cap -> {
            if (cap.getData().getSelectedCharacter() == CharacterType.NONE) return;
            String activeId = cap.getData().getQuestData().getActiveQuestId();
            if (QuestRegistry.SLAY_GRAY_DEMON_ID.equals(activeId)) {
                QuestManager.completeBossKillQuest(killer, QuestRegistry.SLAY_GRAY_DEMON_ID);
            }
        });

        // Server-wide broadcast
        if (killer.getServer() != null) {
            killer.getServer().getPlayerList()
                    .broadcastSystemMessage(
                            Component.literal("The Gray Demon has been defeated."), false);
        }
    }
}
