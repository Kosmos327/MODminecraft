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

import java.util.List;

/**
 * Gowther (Lust) ability — Memory Rewrite.
 *
 * <p>Gowther forcibly rewrites the memories of all nearby enemies, inflicting
 * strong confusion, blindness, and weakness, leaving them disoriented and
 * unable to fight effectively.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#MEMORY_REWRITE}</li>
 *   <li>Mana cost: 50</li>
 *   <li>Cooldown: 200 ticks (10 s)</li>
 *   <li>Duration: 5 seconds (100 ticks)</li>
 * </ul>
 */
public class GowtherMemoryRewriteAbility extends Ability {

    private static final double RADIUS = 6.0;
    /** Duration of the control effects in ticks (5 seconds). */
    private static final int DURATION_TICKS = 100;

    public GowtherMemoryRewriteAbility() {
        super(AbilityType.MEMORY_REWRITE, 50, 200);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        AABB searchBox = player.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity instanceof Enemy && entity != player
        );

        for (LivingEntity target : targets) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, DURATION_TICKS, 1, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, DURATION_TICKS, 0, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DURATION_TICKS, 1, false, true));
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_PREPARE_SUMMON,
                SoundSource.PLAYERS,
                1.0f,
                0.6f
        );
    }
}
