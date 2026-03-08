package com.sevensins.ability.impl.gluttony;

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
 * Merlin (Gluttony) ability — Arcane Burst.
 *
 * <p>Merlin releases a concentrated burst of arcane energy in front of her,
 * dealing magic damage to all enemies caught in the blast.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#ARCANE_BURST}</li>
 *   <li>Mana cost: 30</li>
 *   <li>Cooldown: 120 ticks (6 s)</li>
 *   <li>Damage: 11</li>
 * </ul>
 */
public class MerlinArcaneBurstAbility extends Ability {

    private static final double RANGE = 6.0;
    private static final double BOX_HALF_WIDTH = 2.0;
    private static final float DAMAGE = 11.0f;

    public MerlinArcaneBurstAbility() {
        super(AbilityType.ARCANE_BURST, 30, 120);
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
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.ARCANE_BURST, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
        }

        // Spawn magic particles along the burst path
        ServerLevel serverLevel = (ServerLevel) player.level();
        for (int i = 1; i <= 5; i++) {
            Vec3 pos = eyes.add(look.scale(i * (RANGE / 5.0)));
            serverLevel.sendParticles(
                    ParticleTypes.WITCH,
                    pos.x, pos.y, pos.z,
                    5,
                    0.3, 0.3, 0.3,
                    0.05
            );
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }
}
