package com.sevensins.event;

import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.OpenCharacterSelectionPacket;
import com.sevensins.story.StoryTriggerService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Server-side event handler for player login.
 * <p>
 * When a player logs in for the first time (or whenever their
 * {@code selectedCharacter} is still {@link CharacterType#NONE}), a
 * {@link OpenCharacterSelectionPacket} is sent to that specific client.
 * The client then opens {@link com.sevensins.client.screen.CharacterSelectionScreen}.
 * <p>
 * If the player already has a character but has not yet received the first
 * story quest (e.g. they joined before this system was added, or they
 * relogged before completing it), the quest is re-assigned here.
 * <p>
 * The GUI is never opened directly from the server — all screen logic is
 * handled via the dedicated network packet, which keeps the architecture
 * valid for both dedicated servers and LAN worlds.
 */
@Mod.EventBusSubscriber(modid = "seven_sins", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerLoginEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        // This event fires on the logical server (both dedicated and integrated/LAN).
        // Only ServerPlayer instances can receive packets.
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ModCapabilities.get(serverPlayer).ifPresent(data -> {
            CharacterType character = data.getData().getSelectedCharacter();
            if (character == CharacterType.NONE) {
                // No character chosen yet — open the selection screen
                ModNetwork.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> serverPlayer),
                        new OpenCharacterSelectionPacket()
                );
            } else {
                // Character already chosen — ensure first quest is assigned if not done yet
                StoryTriggerService.getInstance()
                        .checkAndAssignFirstQuest(serverPlayer, data.getData());
            }
        });
    }
}

