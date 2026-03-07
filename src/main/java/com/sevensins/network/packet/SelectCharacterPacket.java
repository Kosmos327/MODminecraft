package com.sevensins.network.packet;

import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.SyncCharacterDataPacket;
import com.sevensins.story.StoryTriggerService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Sent from a client to the server when a player clicks one of the character
 * buttons in {@link com.sevensins.client.screen.CharacterSelectionScreen}.
 *
 * <p>The server validates the request and writes the selection into the player's
 * {@link PlayerCharacterData} capability.  No state is mutated on the client.</p>
 */
public class SelectCharacterPacket {

    private final CharacterType character;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public SelectCharacterPacket(CharacterType character) {
        if (character == null) throw new NullPointerException("character must not be null");
        this.character = character;
    }

    // -------------------------------------------------------------------------
    // Encode / Decode
    // -------------------------------------------------------------------------

    /**
     * Encodes this packet into a byte buffer that will be sent over the network.
     */
    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(character);
    }

    /**
     * Decodes a packet from the byte buffer received from the network.
     */
    public static SelectCharacterPacket decode(FriendlyByteBuf buf) {
        CharacterType character = buf.readEnum(CharacterType.class);
        return new SelectCharacterPacket(character);
    }

    // -------------------------------------------------------------------------
    // Handle
    // -------------------------------------------------------------------------

    /**
     * Handles the packet on the <strong>server</strong> thread.
     *
     * <ul>
     *   <li>Retrieves the {@link PlayerCharacterData} capability from the sender.</li>
     *   <li>If the player has not yet selected a character, records the chosen
     *       character and triggers {@link StoryTriggerService#onCharacterSelected}
     *       to begin Chapter 1 and assign the first quest.</li>
     * </ul>
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();

        // Only execute server-side logic – never on the client.
        if (ctx.getDirection().getReceptionSide().isClient()) {
            ctx.setPacketHandled(true);
            return;
        }

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ModCapabilities.get(player).ifPresent(capData -> {
                    PlayerCharacterData data = capData.getData();
                    // Allow selection only if the player hasn't chosen yet.
                    if (data.getSelectedCharacter() == CharacterType.NONE) {
                        data.setSelectedCharacter(character);

                        // Trigger story Chapter 1 and first quest (sets personalStoryStage
                        // via StoryChapter.AWAKENING for all characters uniformly).
                        StoryTriggerService.getInstance().onCharacterSelected(player, character);

                        // Sync the updated character data (including selectedCharacter) to the client
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new SyncCharacterDataPacket(data));
                    }
                });
        });

        ctx.setPacketHandled(true);
    }
}
