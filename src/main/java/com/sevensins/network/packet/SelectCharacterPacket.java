package com.sevensins.network.packet;

import com.sevensins.common.capability.ModCapabilities;
import com.sevensins.common.data.CharacterType;
import com.sevensins.common.data.PlayerCharacterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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
     *   <li>If the player has not yet selected a character (or if selection is
     *       still permitted on first login), records the chosen character.</li>
     *   <li>If the chosen character is {@link CharacterType#MELIODAS}, sets
     *       {@code personalStoryStage = 1} to kick off Meliodas's personal arc.</li>
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

            player.getCapability(ModCapabilities.PLAYER_CHARACTER).ifPresent(data -> {
                // Allow selection only if the player hasn't chosen yet.
                if (!data.hasSelectedCharacter()) {
                    data.setSelectedCharacter(character);

                    // Meliodas special-case: begin personal story arc.
                    if (character == CharacterType.MELIODAS) {
                        data.setPersonalStoryStage(1);
                    }
                }
            });
        });

        ctx.setPacketHandled(true);
    }
}
