package com.sevensins.network.packet;

import com.sevensins.ability.AbilityType;
import com.sevensins.client.hud.CooldownHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Server → Client packet that syncs ability cooldown expiry times to the client
 * so the {@link CooldownHudOverlay} can display live cooldown countdowns.
 *
 * <p>Sent after a successful ability activation via
 * {@link UseAbilityPacket}.</p>
 */
public class SyncCooldownPacket {

    /** Ability → absolute expiry time in milliseconds ({@link System#currentTimeMillis()}). */
    private final Map<AbilityType, Long> expiryTimes;

    public SyncCooldownPacket(Map<AbilityType, Long> expiryTimes) {
        this.expiryTimes = new EnumMap<>(AbilityType.class);
        this.expiryTimes.putAll(expiryTimes);
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(SyncCooldownPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.expiryTimes.size());
        packet.expiryTimes.forEach((ability, expiry) -> {
            buf.writeEnum(ability);
            buf.writeLong(expiry);
        });
    }

    public static SyncCooldownPacket decode(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<AbilityType, Long> map = new EnumMap<>(AbilityType.class);
        for (int i = 0; i < size; i++) {
            AbilityType ability = buf.readEnum(AbilityType.class);
            long expiry = buf.readLong();
            map.put(ability, expiry);
        }
        return new SyncCooldownPacket(map);
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the client)
    // -------------------------------------------------------------------------

    public static void handle(SyncCooldownPacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> handleOnClient(packet)));
        ctx.setPacketHandled(true);
    }

    private static void handleOnClient(SyncCooldownPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        CooldownHudOverlay.updateCooldowns(packet.expiryTimes);
    }
}
