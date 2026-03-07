package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.quest.QuestManager;
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
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only count hostile mobs (Monster subclasses)
        if (!(event.getEntity() instanceof Monster)) return;

        // Killer must be a server-side player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        // Player must have a character selected
        ModCapabilities.get(killer).ifPresent(cap -> {
            if (cap.getData().getSelectedCharacter() == CharacterType.NONE) return;
            QuestManager.incrementKillProgress(killer);
        });
    }
}
