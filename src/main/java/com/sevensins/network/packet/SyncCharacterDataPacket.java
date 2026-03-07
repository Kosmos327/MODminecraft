package com.sevensins.network.packet;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Server → Client packet that synchronises the skill-tree fields of a player's
 * {@link PlayerCharacterData} capability: {@code skillPoints} and
 * {@code unlockedAbilities}.
 *
 * <p>Sent after a successful skill unlock so the client GUI reflects the new
 * state immediately.</p>
 */
public class SyncCharacterDataPacket {

    private final int skillPoints;
    private final Set<AbilityType> unlockedAbilities;

    public SyncCharacterDataPacket(int skillPoints, Set<AbilityType> unlockedAbilities) {
        this.skillPoints = skillPoints;
        this.unlockedAbilities = EnumSet.copyOf(
                unlockedAbilities.isEmpty() ? EnumSet.noneOf(AbilityType.class) : unlockedAbilities);
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(SyncCharacterDataPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.skillPoints);
        buf.writeInt(packet.unlockedAbilities.size());
        for (AbilityType ability : packet.unlockedAbilities) {
            buf.writeEnum(ability);
        }
    }

    public static SyncCharacterDataPacket decode(FriendlyByteBuf buf) {
        int skillPoints = buf.readInt();
        int size = buf.readInt();
        Set<AbilityType> abilities = EnumSet.noneOf(AbilityType.class);
        for (int i = 0; i < size; i++) {
            abilities.add(buf.readEnum(AbilityType.class));
        }
        return new SyncCharacterDataPacket(skillPoints, abilities);
    }

    // -------------------------------------------------------------------------
    // Handler (runs on the client)
    // -------------------------------------------------------------------------

    public static void handle(SyncCharacterDataPacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> handleOnClient(packet)));
        ctx.setPacketHandled(true);
    }

    private static void handleOnClient(SyncCharacterDataPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            data.setSkillPoints(packet.skillPoints);
            data.setUnlockedAbilities(packet.unlockedAbilities);
        });
    }
}
