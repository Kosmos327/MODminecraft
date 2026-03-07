package com.sevensins.ability.impl;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.UltimateAbilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Escanor (Pride) ultimate ability — The One.
 *
 * <p>Activating The One unleashes Escanor's peak noon power for a limited
 * duration, granting extreme offensive and defensive bonuses:
 * <ul>
 *   <li>Major damage boost (Strength III via MobEffect)</li>
 *   <li>Resistance II throughout the form</li>
 * </ul>
 *
 * <p>Stats:
 * <ul>
 *   <li>Type: {@link AbilityType#THE_ONE}</li>
 *   <li>Mana cost: 100</li>
 *   <li>Cooldown: 180 seconds (3600 ticks)</li>
 *   <li>Duration: 25 seconds (500 ticks)</li>
 * </ul>
 */
public class TheOneAbility extends Ability {

    /** Duration of The One form in game ticks (25 seconds). */
    private static final int DURATION_TICKS = 25 * 20;

    public TheOneAbility() {
        // 100 mana cost, 180-second (3600-tick) cooldown
        super(AbilityType.THE_ONE, 100, 180 * 20);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        UltimateAbilityManager.startUltimate(serverPlayer, AbilityType.THE_ONE, DURATION_TICKS);
    }
}
