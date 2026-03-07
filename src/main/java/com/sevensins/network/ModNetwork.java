package com.sevensins.network;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.capability.ISinData;
import com.sevensins.network.packet.AdvanceDialoguePacket;
import com.sevensins.network.packet.SelectCharacterPacket;
import com.sevensins.network.packet.SinDataSyncPacket;
import com.sevensins.network.packet.SyncBossStatePacket;
import com.sevensins.network.packet.SyncCharacterDataPacket;
import com.sevensins.network.packet.SyncCooldownPacket;
import com.sevensins.network.packet.TriggerDialoguePacket;
import com.sevensins.network.packet.UnlockSkillPacket;
import com.sevensins.network.packet.UseAbilityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Registers and exposes the mod's networking channel.
 *
 * <p>All packets must be registered in {@link #register()}, which is called during
 * {@link net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent}.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SevenSinsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextPacketId = 0;

    /** Called from {@link SevenSinsMod#commonSetup} via {@code enqueueWork}. */
    public static void register() {
        CHANNEL.registerMessage(
                nextPacketId++,
                SinDataSyncPacket.class,
                SinDataSyncPacket::encode,
                SinDataSyncPacket::decode,
                SinDataSyncPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                OpenCharacterSelectionPacket.class,
                OpenCharacterSelectionPacket::encode,
                OpenCharacterSelectionPacket::decode,
                OpenCharacterSelectionPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                SelectCharacterPacket.class,
                SelectCharacterPacket::encode,
                SelectCharacterPacket::decode,
                SelectCharacterPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                UseAbilityPacket.class,
                UseAbilityPacket::encode,
                UseAbilityPacket::decode,
                UseAbilityPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                UnlockSkillPacket.class,
                UnlockSkillPacket::encode,
                UnlockSkillPacket::decode,
                UnlockSkillPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                SyncCharacterDataPacket.class,
                SyncCharacterDataPacket::encode,
                SyncCharacterDataPacket::decode,
                SyncCharacterDataPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                SyncCooldownPacket.class,
                SyncCooldownPacket::encode,
                SyncCooldownPacket::decode,
                SyncCooldownPacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                TriggerDialoguePacket.class,
                TriggerDialoguePacket::encode,
                TriggerDialoguePacket::decode,
                TriggerDialoguePacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                AdvanceDialoguePacket.class,
                AdvanceDialoguePacket::encode,
                AdvanceDialoguePacket::decode,
                AdvanceDialoguePacket::handle
        );
        CHANNEL.registerMessage(
                nextPacketId++,
                SyncBossStatePacket.class,
                SyncBossStatePacket::encode,
                SyncBossStatePacket::decode,
                SyncBossStatePacket::handle
        );
    }

    /**
     * Sends the player's current {@link ISinData} to their client.
     * Must only be called from the server thread.
     */
    public static void syncToPlayer(ISinData data, ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SinDataSyncPacket(data));
    }
}
