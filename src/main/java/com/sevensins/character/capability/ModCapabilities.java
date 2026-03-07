package com.sevensins.character.capability;

import com.sevensins.SevenSinsMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Declares and registers all Forge capabilities used by this mod.
 * Registered on the mod event bus via {@link Mod.EventBusSubscriber}.
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    /**
     * The capability token for {@link IPlayerCharacterData} (character selection system).
     */
    public static final Capability<IPlayerCharacterData> PLAYER_CHARACTER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    /**
     * The capability token for {@link ISinData} (sin alignment system).
     * This field is valid (non-null) after class load; it becomes active once
     * {@link RegisterCapabilitiesEvent} fires and registers the type.
     */
    public static final Capability<ISinData> SIN_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerCharacterData.class);
        event.register(ISinData.class);
    }

    /** Convenience accessor for the {@link IPlayerCharacterData} capability. */
    public static LazyOptional<IPlayerCharacterData> get(Player player) {
        return player.getCapability(PLAYER_CHARACTER_DATA);
    }
}
