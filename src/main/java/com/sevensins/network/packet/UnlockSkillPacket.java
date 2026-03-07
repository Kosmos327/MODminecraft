package com.sevensins.network.packet;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.character.skilltree.SkillUnlockManager;
import com.sevensins.network.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Client → Server packet sent when a player clicks an "Unlock" button in the
 * {@link com.sevensins.client.screen.SkillTreeScreen}.
 *
 * <p>The server validates the request using {@link SkillUnlockManager}.
 * On success it deducts skill points, records the ability as unlocked, and
 * sends a {@link SyncCharacterDataPacket} back so the client stays in sync.</p>
 */
public class UnlockSkillPacket {

    private final AbilityType abilityType;

    public UnlockSkillPacket(AbilityType abilityType) {
        if (abilityType == null) throw new NullPointerException("abilityType must not be null");
        this.abilityType = abilityType;
    }

    // -------------------------------------------------------------------------
    // Codec
    // -------------------------------------------------------------------------

    public static void encode(UnlockSkillPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.abilityType);
    }

    public static UnlockSkillPacket decode(FriendlyByteBuf buf) {
        return new UnlockSkillPacket(buf.readEnum(AbilityType.class));
    }

    // -------------------------------------------------------------------------
    // Server-side handler
    // -------------------------------------------------------------------------

    public static void handle(UnlockSkillPacket packet,
                               Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();

        if (ctx.getDirection().getReceptionSide().isClient()) {
            ctx.setPacketHandled(true);
            return;
        }

        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            SkillUnlockManager.UnlockResult result =
                    SkillUnlockManager.tryUnlock(player, packet.abilityType);

            switch (result) {
                case SUCCESS -> {
                    // Inform the player
                    player.sendSystemMessage(Component.literal(
                            "Unlocked: " + capitalize(packet.abilityType.name())));

                    // Sync updated skill data back to client
                    ModCapabilities.get(player).ifPresent(cap -> {
                        ModNetwork.CHANNEL.send(
                                PacketDistributor.PLAYER.with(() -> player),
                                new SyncCharacterDataPacket(cap.getData())
                        );
                    });
                }
                case ALREADY_UNLOCKED ->
                        player.sendSystemMessage(Component.literal(
                                "You have already unlocked " + capitalize(packet.abilityType.name()) + "."));
                case NOT_ENOUGH_POINTS ->
                        player.sendSystemMessage(Component.literal(
                                "Not enough skill points to unlock " + capitalize(packet.abilityType.name()) + "."));
                case MISSING_PREREQUISITE ->
                        player.sendSystemMessage(Component.literal(
                                "You must unlock the prerequisite ability first."));
                case NOT_IN_TREE ->
                        player.sendSystemMessage(Component.literal(
                                "That ability does not belong to your skill tree."));
                case NO_CHARACTER ->
                        player.sendSystemMessage(Component.literal(
                                "You have not selected a character yet."));
            }
        });

        ctx.setPacketHandled(true);
    }

    // -------------------------------------------------------------------------

    private static String capitalize(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.substring(0, 1).toUpperCase() +
               name.substring(1).toLowerCase().replace('_', ' ');
    }
}
