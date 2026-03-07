package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
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
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QuestEvents {

    private QuestEvents() {}

    /**
     * Increments the active KILL quest's progress when a hostile mob is killed
     * by the player.
     *
     * <p>For the {@value QuestRegistry#FIRST_DEMON_HUNT_ID} quest, only mobs
     * with a max-health of at least 20 are counted as "powerful".</p>
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only count hostile mobs (Monster subclasses)
        if (!(event.getEntity() instanceof Monster monster)) return;

        // Killer must be a server-side player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

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
}
