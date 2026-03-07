package com.sevensins.network.packet;

import com.sevensins.boss.BossManager;
import com.sevensins.boss.BossPhase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Server → Client packet that synchronises boss health and phase to the client
 * so the {@link com.sevensins.client.hud.BossHealthOverlay} can render
 * up-to-date information.
 *
 * <p>When {@code alive} is {@code false} the packet signals that no boss is
 * currently active and the overlay should be hidden.</p>
 *
 * <p>Use {@link #clear()} to create a "hide overlay" packet.</p>
 */
public class SyncBossStatePacket {

    private final boolean alive;
    @Nullable private final String name;
    private final float currentHp;
    private final float maxHp;
    private final BossPhase phase;

    /** Constructs a packet that shows a live boss state. */
    public SyncBossStatePacket(String name, float currentHp, float maxHp, BossPhase phase) {
        this.alive = true;
        this.name = name;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
        this.phase = phase;
    }

    private SyncBossStatePacket() {
        this.alive = false;
        this.name = null;
        this.currentHp = 0;
        this.maxHp = 0;
        this.phase = BossPhase.PHASE_1;
    }

    /** Returns a packet that tells the client to hide the boss overlay. */
    public static SyncBossStatePacket clear() {
        return new SyncBossStatePacket();
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(SyncBossStatePacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.alive);
        if (packet.alive) {
            buf.writeUtf(packet.name != null ? packet.name : "");
            buf.writeFloat(packet.currentHp);
            buf.writeFloat(packet.maxHp);
            buf.writeEnum(packet.phase);
        }
    }

    public static SyncBossStatePacket decode(FriendlyByteBuf buf) {
        boolean alive = buf.readBoolean();
        if (!alive) return clear();
        String name = buf.readUtf();
        float currentHp = buf.readFloat();
        float maxHp = buf.readFloat();
        BossPhase phase = buf.readEnum(BossPhase.class);
        return new SyncBossStatePacket(name, currentHp, maxHp, phase);
    }

    // -------------------------------------------------------------------------
    // Handler
    // -------------------------------------------------------------------------

    public static void handle(SyncBossStatePacket packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleOnClient(packet)));
        context.setPacketHandled(true);
    }

    private static void handleOnClient(SyncBossStatePacket packet) {
        if (!packet.alive) {
            BossManager.setClientBossState(null);
        } else {
            BossManager.setClientBossState(
                    new BossManager.BossInfo(packet.name, packet.currentHp, packet.maxHp, packet.phase));
        }
    }
}
