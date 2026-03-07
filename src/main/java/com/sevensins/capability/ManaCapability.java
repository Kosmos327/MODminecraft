package com.sevensins.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Registers and exposes the {@link IMana} capability token.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ManaCapability {

    public static final Capability<IMana> MANA_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IMana.class);
    }

    // -------------------------------------------------------------------------
    // Default implementation

    public static IMana createDefault() {
        return new IMana() {
            private int mana = 100;
            private final int maxMana = 100;

            @Override
            public int getMana() {
                return mana;
            }

            @Override
            public void setMana(int mana) {
                this.mana = Math.max(0, Math.min(mana, maxMana));
            }

            @Override
            public int getMaxMana() {
                return maxMana;
            }
        };
    }
}
