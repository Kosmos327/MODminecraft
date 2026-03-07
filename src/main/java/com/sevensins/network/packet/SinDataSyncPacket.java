package com.sevensins.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client packet that synchronises the full {@code ISinData} state to
 * the owning player.
 */
public class SinDataSyncPacket {

    // Placeholder payload – expand fields as ISinData grows.
    private final String characterName;

    public SinDataSyncPacket(String characterName) {
        this.characterName = characterName;
    }

    // -------------------------------------------------------------------------

    public static SinDataSyncPacket decode(FriendlyByteBuf buf) {
        return new SinDataSyncPacket(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(characterName == null ? "" : characterName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // Client-side: update local capability cache
            // TODO: resolve client-side ISinData and apply characterName
        });
        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------

    public String getCharacterName() {
        return characterName;
    }
}
