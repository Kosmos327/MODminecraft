package com.sevensins.ability.impl.greed;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import com.sevensins.config.BalanceHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
 * Ban (Greed) ability — Hunter Fest.
 *
 * <p>Unleashes a powerful burst of greed energy around the player, dealing
 * heavy damage to all nearby enemies and applying knockback, representing
 * Ban's ultimate hunting assault.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#HUNTER_FEST}</li>
 *   <li>Mana cost: 50</li>
 *   <li>Cooldown: 200 ticks (10 s)</li>
 *   <li>Damage: 18</li>
 * </ul>
 */
public class BanHunterFestAbility extends Ability {

    private static final double RADIUS = 5.0;
    private static final float DAMAGE = 18.0f;
    private static final double KNOCKBACK_HORIZONTAL = 1.8;
    private static final double KNOCKBACK_VERTICAL = 0.5;

    public BanHunterFestAbility() {
        super(AbilityType.HUNTER_FEST, 50, 200);
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
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.HUNTER_FEST, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
            Vec3 delta = target.position().subtract(player.position());
            if (delta.lengthSqr() > 1.0e-8) {
                Vec3 dir = delta.normalize();
                target.addDeltaMovement(new Vec3(
                        dir.x * KNOCKBACK_HORIZONTAL,
                        KNOCKBACK_VERTICAL,
                        dir.z * KNOCKBACK_HORIZONTAL
                ));
            }
        }

        // Spawn burst particles
        ServerLevel serverLevel = (ServerLevel) player.level();
        Vec3 center = player.position().add(0, 1, 0);
        serverLevel.sendParticles(
                ParticleTypes.CRIT,
                center.x, center.y, center.z,
                30,
                RADIUS * 0.5, 1.0, RADIUS * 0.5,
                0.1
        );

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                1.0f,
                1.2f
        );
    }
}
