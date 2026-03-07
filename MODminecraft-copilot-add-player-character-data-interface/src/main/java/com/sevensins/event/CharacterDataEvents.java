package com.sevensins.event;

import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.character.capability.PlayerCharacterDataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "seven_sins")
public class CharacterDataEvents {

    private static final ResourceLocation PLAYER_CHARACTER_DATA_KEY =
            new ResourceLocation("seven_sins", "player_character_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        if (event.getCapabilities().containsKey(PLAYER_CHARACTER_DATA_KEY)) {
            return;
        }
        PlayerCharacterDataProvider provider = new PlayerCharacterDataProvider();
        event.addCapability(PLAYER_CHARACTER_DATA_KEY, provider);
        event.addListener(provider::invalidate);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player player = event.getEntity();

        original.reviveCaps();
        try {
            ModCapabilities.get(original).ifPresent(oldData ->
                    ModCapabilities.get(player).ifPresent(newData ->
                            newData.copyFrom(oldData.getData())
                    )
            );
        } finally {
            original.invalidateCaps();
        }
    }
}
