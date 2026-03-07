package com.sevensins.character.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;

public class ModCapabilities {

    public static final Capability<IPlayerCharacterData> PLAYER_CHARACTER_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(IPlayerCharacterData.class);
    }

    public static LazyOptional<IPlayerCharacterData> get(Player player) {
        return player.getCapability(PLAYER_CHARACTER_DATA);
    }
}
