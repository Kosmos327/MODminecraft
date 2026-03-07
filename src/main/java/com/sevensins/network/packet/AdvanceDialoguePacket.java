package com.sevensins.network.packet;

import com.sevensins.dialogue.DialogueManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → Server packet that signals the player has finished reading all
 * dialogue lines in a {@link com.sevensins.client.screen.DialogueScreen}.
 *
 * <p>No payload is needed — the server uses the sender's UUID to look up the
 * active {@link com.sevensins.dialogue.DialogueSession} and execute its action.</p>
 */
public class AdvanceDialoguePacket {

    public AdvanceDialoguePacket() {}

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(AdvanceDialoguePacket packet, FriendlyByteBuf buf) {
        // No payload
    }

    public static AdvanceDialoguePacket decode(FriendlyByteBuf buf) {
        return new AdvanceDialoguePacket();
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the server)
    // -------------------------------------------------------------------------

    public static void handle(AdvanceDialoguePacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();

        if (ctx.getDirection().getReceptionSide().isClient()) {
            ctx.setPacketHandled(true);
            return;
        }

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            DialogueManager.getInstance().onDialogueFinished(player);
        });

        ctx.setPacketHandled(true);
    }
}
