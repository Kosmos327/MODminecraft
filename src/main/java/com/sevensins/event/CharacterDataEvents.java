package com.sevensins.event;

import com.sevensins.ability.CooldownManager;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.character.capability.PlayerCharacterDataProvider;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncCharacterDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = "seven_sins")
public class CharacterDataEvents {

    private static final ResourceLocation PLAYER_CHARACTER_DATA_KEY =
            new ResourceLocation("seven_sins", "player_character_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        if (event.getCapabilities().containsKey(PLAYER_CHARACTER_DATA_KEY)) {
            return;
        }
        PlayerCharacterDataProvider provider = new PlayerCharacterDataProvider();
        event.addCapability(PLAYER_CHARACTER_DATA_KEY, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        original.reviveCaps();
        try {
            ModCapabilities.get(original).ifPresent(oldData ->
                    ModCapabilities.get(player).ifPresent(newData ->
                            newData.copyFrom(oldData.getData())
                    )
            );
        } finally {
            original.invalidateCaps();
        }
    }

    /** Syncs full character data to the client immediately after login. */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToPlayer(player);
    }

    /** Syncs full character data to the client after a respawn. */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToPlayer(player);
    }

    /** Syncs full character data to the client after a dimension change. */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToPlayer(player);
    }

    /**
     * Clears all active cooldowns when a player dies so stale cooldown state
     * does not persist into the next life.
     */
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        CooldownManager.clearCooldowns(player.getUUID());
    }

    // -------------------------------------------------------------------------

    private static void syncToPlayer(ServerPlayer player) {
        ModCapabilities.get(player).ifPresent(cap ->
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SyncCharacterDataPacket(cap.getData())));
    }
}
