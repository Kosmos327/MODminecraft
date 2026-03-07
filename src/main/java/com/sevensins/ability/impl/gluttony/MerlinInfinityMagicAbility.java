package com.sevensins.ability.impl.gluttony;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Merlin (Gluttony) ability — Infinity Magic.
 *
 * <p>Merlin activates her Infinity spell, surrounding herself with an
 * impenetrable magical barrier that grants powerful combat and defensive
 * bonuses for a sustained duration.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#INFINITY_MAGIC}</li>
 *   <li>Mana cost: 70</li>
 *   <li>Cooldown: 300 ticks (15 s)</li>
 *   <li>Duration: 10 seconds (200 ticks)</li>
 * </ul>
 */
public class MerlinInfinityMagicAbility extends Ability {

    /** Duration of the Infinity state in ticks (10 seconds). */
    private static final int DURATION_TICKS = 200;

    public MerlinInfinityMagicAbility() {
        super(AbilityType.INFINITY_MAGIC, 70, 300);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        // Strength II – heavy offensive boost
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, DURATION_TICKS, 1, false, true));
        // Resistance I – defensive protection
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 0, false, true));
        // Speed I – enhanced mobility
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, DURATION_TICKS, 0, false, true));
        // Absorption II – magical barrier health
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION_TICKS, 1, false, true));

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS,
                1.0f,
                1.4f
        );
    }
}
