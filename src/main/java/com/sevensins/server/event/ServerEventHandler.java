package com.sevensins.server.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.common.capability.ISinData;
import com.sevensins.common.capability.ModCapabilities;
import com.sevensins.common.data.SinType;
import com.sevensins.config.ModConfig;
import com.sevensins.network.ModNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side gameplay event handler.
 *
 * <p>Responsible for awarding sin experience based on in-game actions that align
 * with each sin's theme (e.g. Wrath earns XP for kills, Greed for looting, etc.).
 * Triggers a level-up check and syncs data to the client after every change.
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEventHandler {

    /**
     * Awards Wrath experience when the aligned player kills a living entity.
     * Other sins can hook similar events to earn experience relevant to their theme.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player)) return;

        player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
            if (!sinData.isAligned()) return;

            int expGained = 0;

            if (sinData.getActiveSin() == SinType.WRATH) {
                expGained = 10;
            } else if (sinData.getActiveSin() == SinType.PRIDE) {
                // Pride earns XP by asserting dominance (killing any mob)
                expGained = 5;
            }

            if (expGained > 0) {
                sinData.addSinExperience(expGained);
                checkAndApplyLevelUp(sinData, player);
                ModNetwork.syncToPlayer(sinData, player);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Checks whether the player has enough experience to advance a sin level.
     * Repeats until the player no longer has enough experience or hits the cap.
     */
    private static void checkAndApplyLevelUp(ISinData sinData, ServerPlayer player) {

        int maxLevel = ModConfig.SIN_LEVEL_MAX.get();
        int expPerLevel = ModConfig.SIN_EXPERIENCE_PER_LEVEL.get();

        while (sinData.getSinLevel() < maxLevel
                && sinData.getSinExperience() >= expPerLevel) {
            sinData.setSinExperience(sinData.getSinExperience() - expPerLevel);
            sinData.setSinLevel(sinData.getSinLevel() + 1);

            player.displayClientMessage(
                    Component.translatable(
                            "message.seven_sins.sin_level_up",
                            Component.translatable(
                                    sinData.getActiveSin().getTranslationKey()),
                            sinData.getSinLevel()),
                    false);

            SevenSinsMod.LOGGER.debug("[SevenSins] Player {} levelled up to sin level {}",
                    player.getName().getString(), sinData.getSinLevel());
        }
    }
}
