package com.sevensins.common.capability;

import com.sevensins.character.CharacterType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers and exposes the {@link ISinData} capability token used throughout
 * the mod.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    public static final Capability<ISinData> SIN_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ISinData.class);
    }

    // -------------------------------------------------------------------------
    // Default (in-memory) implementation used by SinDataProvider

    public static ISinData createDefaultSinData() {
        return new ISinData() {
            private CharacterType character = null;

            @Override
            public CharacterType getCharacter() {
                return character;
            }

            @Override
            public void setCharacter(CharacterType character) {
                this.character = character;
            }
        };
    }
}
