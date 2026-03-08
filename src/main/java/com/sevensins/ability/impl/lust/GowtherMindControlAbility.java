package com.sevensins.ability.impl.lust;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Gowther (Lust) ability — Mind Control.
 *
 * <p>Gowther fires a mental arrow that confuses and slows nearby enemies,
 * representing his ability to manipulate the minds of those around him.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#MIND_CONTROL}</li>
 *   <li>Mana cost: 30</li>
 *   <li>Cooldown: 120 ticks (6 s)</li>
 * </ul>
 */
public class GowtherMindControlAbility extends Ability {

    private static final double RANGE = 5.0;
    private static final double BOX_HALF_WIDTH = 2.0;
    /** Duration of the control effects in ticks (4 seconds). */
    private static final int DURATION_TICKS = 80;

    public GowtherMindControlAbility() {
        super(AbilityType.MIND_CONTROL, 30, 120);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eyes.add(look.scale(RANGE));
        AABB searchBox = new AABB(eyes, end).inflate(BOX_HALF_WIDTH);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity instanceof Enemy && entity != player
        );

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, DURATION_TICKS, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION_TICKS, 1, false, true));
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_PREPARE_ATTACK,
                SoundSource.PLAYERS,
                1.0f,
                0.8f
        );
    }
}
