package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.world.DungeonManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side event handler that forwards mob-death events to
 * {@link DungeonManager} so it can track dungeon-clear progress.
 *
 * <p>Runs at {@link EventPriority#LOW} so that normal kill-quest processing
 * in {@link QuestEvents} completes first at {@code NORMAL} priority.</p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonEvents {

    private DungeonEvents() {}

    /**
     * Called after every living-entity death.  If the dead entity is tracked
     * by an active dungeon run, delegates to
     * {@link DungeonManager#onTrackedMobDied}.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity dying = event.getEntity();
        if (!DungeonManager.getInstance().isTrackedMob(dying.getUUID())) return;

        // Resolve the killer — may be null if the mob died to the environment
        ServerPlayer killer = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp) {
            killer = sp;
        }

        DungeonManager.getInstance().onTrackedMobDied(dying.getUUID(), killer);
    }
}
