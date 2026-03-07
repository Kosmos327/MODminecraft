package com.sevensins.network;

import com.sevensins.client.screen.CharacterSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Packet sent from the server to a client to instruct it to open the
 * {@link CharacterSelectionScreen}. No payload is needed — the server only
 * signals that the screen must be opened; all character data is already
 * available on the client via the synced capability.
 */
public class OpenCharacterSelectionPacket {

    public OpenCharacterSelectionPacket() {}

    public static void encode(OpenCharacterSelectionPacket pkt, FriendlyByteBuf buf) {
        // No payload required.
    }

    public static OpenCharacterSelectionPacket decode(FriendlyByteBuf buf) {
        return new OpenCharacterSelectionPacket();
    }

    public static void handle(OpenCharacterSelectionPacket pkt, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHandler::openScreen)
        );
        ctx.setPacketHandled(true);
    }

    /**
     * Isolated inner class so that {@link Minecraft} and client-only classes are
     * never loaded on the dedicated server.
     */
    private static class ClientHandler {
        static void openScreen() {
            Minecraft.getInstance().setScreen(new CharacterSelectionScreen());
        }
    }
}
