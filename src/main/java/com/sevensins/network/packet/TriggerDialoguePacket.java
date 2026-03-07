package com.sevensins.network.packet;

import com.sevensins.client.screen.DialogueScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → Client packet that carries all lines of a dialogue tree so the
 * client can display them one by one in {@link DialogueScreen}.
 *
 * <p>Each line is a pair of {@code [speakerName, text]}.</p>
 */
public class TriggerDialoguePacket {

    private final List<String[]> lines;

    /**
     * @param lines ordered list of dialogue lines; each element is a two-element
     *              array {@code {speakerName, text}}
     */
    public TriggerDialoguePacket(List<String[]> lines) {
        this.lines = List.copyOf(lines);
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(TriggerDialoguePacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.lines.size());
        for (String[] line : packet.lines) {
            buf.writeUtf(line[0]);
            buf.writeUtf(line[1]);
        }
    }

    public static TriggerDialoguePacket decode(FriendlyByteBuf buf) {
        int count = buf.readInt();
        List<String[]> lines = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String speaker = buf.readUtf();
            String text = buf.readUtf();
            lines.add(new String[]{ speaker, text });
        }
        return new TriggerDialoguePacket(lines);
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the client)
    // -------------------------------------------------------------------------

    public static void handle(TriggerDialoguePacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> handleOnClient(packet)));
        ctx.setPacketHandled(true);
    }

    private static void handleOnClient(TriggerDialoguePacket packet) {
        Minecraft.getInstance().setScreen(new DialogueScreen(packet.lines));
    }
}
