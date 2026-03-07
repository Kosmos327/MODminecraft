package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.character.capability.SinDataProvider;
import com.sevensins.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles all capability lifecycle events for the sin alignment system:
 * <ul>
 *   <li>Attaching {@link com.sevensins.character.capability.ISinData} to player entities.</li>
 *   <li>Copying capability data when a player respawns (death clone).</li>
 *   <li>Syncing capability data to the client on login, respawn, and dimension change.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEventHandler {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        if (event.getObject().getCapability(ModCapabilities.SIN_DATA).isPresent()) return;

        SinDataProvider provider = new SinDataProvider();
        event.addCapability(SinDataProvider.ID, provider);
        event.addListener(provider::invalidate);
    }

    /**
     * Copies sin data from the old player instance to the new one.
     * Called after death (clone) and when returning from the End.
     * We copy in both cases so that sin progression is preserved across
     * dimension transitions as well as respawns.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(ModCapabilities.SIN_DATA).ifPresent(oldData ->
                event.getEntity().getCapability(ModCapabilities.SIN_DATA)
                        .ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    /** Syncs capability data to the client after the player logs in. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.SIN_DATA)
                    .ifPresent(data -> ModNetwork.syncToPlayer(data, player));
        }
    }

    /** Syncs capability data to the client after the player respawns. */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.SIN_DATA)
                    .ifPresent(data -> ModNetwork.syncToPlayer(data, player));
        }
    }

    /** Syncs capability data after a player changes dimension. */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(ModCapabilities.SIN_DATA)
                    .ifPresent(data -> ModNetwork.syncToPlayer(data, player));
        }
    }
}
