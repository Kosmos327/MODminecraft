package com.sevensins.network;

import com.sevensins.network.packet.SelectCharacterPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Central registry for all network packets used by the Seven Sins mod.
 *
 * <p>Call {@link #register()} from your mod constructor (or {@code FMLCommonSetupEvent})
 * before any packets can be sent or received.</p>
 *
 * <p>Protocol version {@value #PROTOCOL_VERSION} must match on both client and server.</p>
 */
public final class ModNetwork {

    public static final String PROTOCOL_VERSION = "1";

    /** The network channel shared by all Seven Sins packets. */
    public static SimpleChannel CHANNEL;

    /** Running packet discriminator – increment for every new packet registered. */
    private static int packetId = 0;

    private ModNetwork() {}

    /**
     * Creates the {@link SimpleChannel} and registers all known packets.
     * Must be called once during mod initialisation.
     */
    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                new ResourceLocation("seven_sins", "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        // File 11 – SelectCharacterPacket (client → server)
        CHANNEL.registerMessage(
                packetId++,
                SelectCharacterPacket.class,
                SelectCharacterPacket::encode,
                SelectCharacterPacket::decode,
                SelectCharacterPacket::handle,
                java.util.Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
