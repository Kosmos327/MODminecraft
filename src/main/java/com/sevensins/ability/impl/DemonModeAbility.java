package com.sevensins.ability.impl;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.UltimateAbilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Meliodas (Wrath) ultimate ability — Demon Mode.
 *
 * <p>Activating Demon Mode transforms the player into their demonic state for a
 * limited duration, granting enhanced combat performance:
 * <ul>
 *   <li>Strength II (+100% attack damage via vanilla MobEffect formula)</li>
 *   <li>Speed II (+30% movement speed via MobEffect)</li>
 *   <li>Regeneration I throughout the form</li>
 * </ul>
 *
 * <p>Stats:
 * <ul>
 *   <li>Type: {@link AbilityType#DEMON_MODE}</li>
 *   <li>Mana cost: 80</li>
 *   <li>Cooldown: 120 seconds (2400 ticks)</li>
 *   <li>Duration: 30 seconds (600 ticks)</li>
 * </ul>
 */
public class DemonModeAbility extends Ability {

    /** Duration of the Demon Mode form in game ticks (30 seconds). */
    private static final int DURATION_TICKS = 30 * 20;

    public DemonModeAbility() {
        // 80 mana cost, 120-second (2400-tick) cooldown
        super(AbilityType.DEMON_MODE, 80, 120 * 20);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        UltimateAbilityManager.startUltimate(serverPlayer, AbilityType.DEMON_MODE, DURATION_TICKS);
    }
}
