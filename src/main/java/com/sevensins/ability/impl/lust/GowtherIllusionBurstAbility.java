package com.sevensins.ability.impl.lust;

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
 * Gowther (Lust) ability — Illusion Burst.
 *
 * <p>Gowther fires a burst of illusory magic arrows in his look direction,
 * dealing ranged magic damage to enemies caught in the cone.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#ILLUSION_BURST}</li>
 *   <li>Mana cost: 25</li>
 *   <li>Cooldown: 80 ticks (4 s)</li>
 *   <li>Damage: 9</li>
 * </ul>
 */
public class GowtherIllusionBurstAbility extends Ability {

    private static final double RANGE = 7.0;
    private static final double BOX_HALF_WIDTH = 1.5;
    private static final float DAMAGE = 9.0f;

    public GowtherIllusionBurstAbility() {
        super(AbilityType.ILLUSION_BURST, 25, 80);
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

        DamageSource source = player.damageSources().playerAttack(player);
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.ILLUSION_BURST, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
        }

        // Spawn glowing particles along burst path
        ServerLevel serverLevel = (ServerLevel) player.level();
        for (int i = 1; i <= 5; i++) {
            Vec3 pos = eyes.add(look.scale(i * (RANGE / 5.0)));
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    3,
                    0.2, 0.2, 0.2,
                    0.05
            );
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                1.0f,
                1.2f
        );
    }
}
