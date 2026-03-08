package com.sevensins.ability.impl.sloth;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * King (Sloth) ability — Guardian.
 *
 * <p>King raises Chastiefol as a guardian shield, granting himself Absorption
 * and Resistance for a short duration, representing his defensive spirit-beast
 * form of his sacred treasure.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#GUARDIAN}</li>
 *   <li>Mana cost: 30</li>
 *   <li>Cooldown: 160 ticks (8 s)</li>
 *   <li>Duration: 5 seconds (100 ticks)</li>
 * </ul>
 */
public class KingGuardianAbility extends Ability {

    /** Duration of the defensive buffs in ticks (5 seconds). */
    private static final int DURATION_TICKS = 100;

    public KingGuardianAbility() {
        super(AbilityType.GUARDIAN, 30, 160);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION_TICKS, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 0, false, true));

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.SHIELD_BLOCK,
                SoundSource.PLAYERS,
                1.0f,
                0.8f
        );
    }
}
