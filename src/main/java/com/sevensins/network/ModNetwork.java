package com.sevensins.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("seven_sins", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(
                packetId++,
                OpenCharacterSelectionPacket.class,
                OpenCharacterSelectionPacket::encode,
                OpenCharacterSelectionPacket::decode,
                OpenCharacterSelectionPacket::handle
        );
    }
}
