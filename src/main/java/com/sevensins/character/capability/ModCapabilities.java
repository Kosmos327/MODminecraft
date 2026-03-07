package com.sevensins.character.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "seven_sins", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCapabilities {

    public static final Capability<IPlayerCharacterData> PLAYER_CHARACTER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerCharacterData.class);
    }

    public static LazyOptional<IPlayerCharacterData> get(Player player) {
        return player.getCapability(PLAYER_CHARACTER_DATA);
    }
}
