package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.world.DungeonManager;
import com.sevensins.world.NightRaidManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * Server-side event handler that forwards mob-death events to
 * {@link DungeonManager} and {@link NightRaidManager} so they can track
 * dungeon/raid clear progress.
 *
 * <p>Runs at {@link EventPriority#LOW} so that normal kill-quest processing
 * in {@link QuestEvents} completes first at {@code NORMAL} priority.</p>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DungeonEvents {

    private DungeonEvents() {}

    /**
     * Called after every living-entity death.  If the dead entity is tracked
     * by an active dungeon run or a Night Demon Raid, delegates accordingly.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity dying = event.getEntity();
        UUID mobId = dying.getUUID();

        // Resolve the killer — may be null if the mob died to the environment
        ServerPlayer killer = null;
        Entity attacker = event.getSource().getEntity();
        if (attacker instanceof ServerPlayer sp) {
            killer = sp;
        }

        // Dungeon tracking
        if (DungeonManager.getInstance().isTrackedMob(mobId)) {
            DungeonManager.getInstance().onTrackedMobDied(mobId, killer);
        }

        // Night Raid tracking
        if (NightRaidManager.getInstance().isTrackedMob(mobId)) {
            NightRaidManager.getInstance().onTrackedMobDied(mobId, killer);
        }
    }
}
