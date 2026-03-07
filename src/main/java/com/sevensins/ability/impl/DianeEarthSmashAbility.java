package com.sevensins.ability.impl;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import com.sevensins.config.BalanceHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Diane ability — Earth Smash.
 *
 * <p>Deals damage to all hostile entities within a 4-block radius and applies
 * knockback away from the player.  Runs server-side only; client-side
 * visual effects (particles) are handled separately.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#EARTH_SMASH}</li>
 *   <li>Mana cost: 25</li>
 *   <li>Cooldown: 160 ticks (~8 s)</li>
 * </ul>
 */
public class DianeEarthSmashAbility extends Ability {

    private static final double RADIUS = 4.0;
    private static final float DAMAGE = 10.0f;
    private static final double KNOCKBACK_HORIZONTAL = 1.5;
    private static final double KNOCKBACK_VERTICAL = 0.4;

    public DianeEarthSmashAbility() {
        super(AbilityType.EARTH_SMASH, 25, 160);
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

        DamageSource source = player.damageSources().playerAttack(player);
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.EARTH_SMASH, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);

            Vec3 delta = target.position().subtract(player.position());
            if (delta.lengthSqr() > 1.0e-8) {
                Vec3 knockbackDir = delta.normalize();
                target.addDeltaMovement(new Vec3(
                        knockbackDir.x * KNOCKBACK_HORIZONTAL,
                        KNOCKBACK_VERTICAL,
                        knockbackDir.z * KNOCKBACK_HORIZONTAL
                ));
            }
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0f,
                0.8f
        );
    }
}
