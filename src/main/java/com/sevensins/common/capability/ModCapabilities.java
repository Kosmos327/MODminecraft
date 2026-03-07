package com.sevensins.common.capability;

import com.sevensins.SevenSinsMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Declares and registers all Forge capabilities used by this mod.
 * Registered on the mod event bus via {@link Mod.EventBusSubscriber}.
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    /**
     * The capability token for {@link ISinData}.
     * This field is valid (non-null) after class load; it becomes active once
     * {@link RegisterCapabilitiesEvent} fires and registers the type.
     */
    public static final Capability<ISinData> SIN_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ISinData.class);
    }
}
