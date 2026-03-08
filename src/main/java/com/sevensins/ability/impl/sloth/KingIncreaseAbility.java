package com.sevensins.ability.impl.sloth;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * King (Sloth) ability — Increase.
 *
 * <p>Amplifies King's power through Chastiefol's enhancement form, granting
 * himself a Strength and Speed boost for a short duration.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#INCREASE}</li>
 *   <li>Mana cost: 40</li>
 *   <li>Cooldown: 200 ticks (10 s)</li>
 *   <li>Duration: 6 seconds (120 ticks)</li>
 * </ul>
 */
public class KingIncreaseAbility extends Ability {

    /** Duration of the enhancement buffs in ticks (6 seconds). */
    private static final int DURATION_TICKS = 120;

    public KingIncreaseAbility() {
        super(AbilityType.INCREASE, 40, 200);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, 0, false, true));

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }
}
