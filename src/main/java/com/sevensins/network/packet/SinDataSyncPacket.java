package com.sevensins.network.packet;

import com.sevensins.common.capability.ISinData;
import com.sevensins.common.capability.ModCapabilities;
import com.sevensins.common.capability.SinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → Client packet that synchronises a player's {@link ISinData} capability.
 *
 * <p>Sent on login, respawn, and dimension change, as well as whenever the
 * sin data changes server-side (alignment, level-up, etc.).
 */
public class SinDataSyncPacket {

    private final CompoundTag nbt;

    public SinDataSyncPacket(ISinData sinData) {
        this.nbt = sinData.serializeNBT();
    }

    private SinDataSyncPacket(CompoundTag nbt) {
        this.nbt = nbt;
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(SinDataSyncPacket packet, FriendlyByteBuf buf) {
        buf.writeNbt(packet.nbt);
    }

    public static SinDataSyncPacket decode(FriendlyByteBuf buf) {
        return new SinDataSyncPacket(buf.readNbt());
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the receiving side – always the client for this packet)
    // -------------------------------------------------------------------------

    public static void handle(SinDataSyncPacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                // Guard the Minecraft client reference so the class is safe to load
                // on a dedicated server (where this packet is never received).
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> handleOnClient(packet.nbt)));
        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------
    // Client-only handler
    // -------------------------------------------------------------------------

    private static void handleOnClient(CompoundTag nbt) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
            if (sinData instanceof SinData concrete) {
                concrete.deserializeNBT(nbt);
            }
        });
    }
}
