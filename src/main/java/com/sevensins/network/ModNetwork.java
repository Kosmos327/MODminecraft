package com.sevensins.network;

import com.sevensins.SevenSinsMod;
import com.sevensins.network.packet.SinDataSyncPacket;
import com.sevensins.network.packet.UseAbilityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central registration of the mod's network channel and all packets.
 */
public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SevenSinsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int nextId = 0;

    /** Called once during {@code FMLCommonSetupEvent}. */
    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                SinDataSyncPacket.class,
                SinDataSyncPacket::encode,
                SinDataSyncPacket::decode,
                SinDataSyncPacket::handle
        );

        CHANNEL.registerMessage(
                nextId++,
                UseAbilityPacket.class,
                UseAbilityPacket::encode,
                UseAbilityPacket::decode,
                UseAbilityPacket::handle
        );
    }
}
