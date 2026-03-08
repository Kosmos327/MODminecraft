package com.sevensins.event;

import com.sevensins.ability.PassiveAbilityManager;
import com.sevensins.mana.ManaRegenService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event subscriber responsible for passive mana regeneration.
 *
 * <p>Every {@value #REGEN_INTERVAL_TICKS} ticks (roughly once per second) each
 * server-side player regenerates {@value #MANA_PER_REGEN} mana, up to their
 * maximum.</p>
 *
 * <p>Actual regen logic (including the max-mana skip) is handled by
 * {@link ManaRegenService#applyRegen} so the optimisation is shared with any
 * future callers.</p>
 */
@Mod.EventBusSubscriber(modid = "seven_sins")
public class ManaRegenEvents {

    /** Number of ticks between each regen tick. */
    private static final int REGEN_INTERVAL_TICKS = 20;

    /** Base amount of mana restored each regen tick. */
    private static final int MANA_PER_REGEN = 2;

    private ManaRegenEvents() {}

    /**
     * Handles {@link TickEvent.PlayerTickEvent} to apply periodic mana regen.
     *
     * <p>Only runs on the server side ({@link TickEvent.Phase#END}) so that
     * mana values are authoritative and never processed twice.</p>
     *
     * @param event the player tick event fired by Forge
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (serverPlayer.tickCount % REGEN_INTERVAL_TICKS != 0) {
            return;
        }

        // Apply DIANE (Envy) passive mana-regen bonus
        int effectiveRegen = MANA_PER_REGEN;
        float regenBonus = PassiveAbilityManager.getManaRegenBonus(serverPlayer);
        if (regenBonus > 0f) {
            effectiveRegen = Math.round(MANA_PER_REGEN * (1f + regenBonus));
        }

        ManaRegenService.applyRegen(serverPlayer, effectiveRegen);
    }
}
