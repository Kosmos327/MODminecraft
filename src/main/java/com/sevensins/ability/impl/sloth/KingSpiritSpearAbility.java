package com.sevensins.ability.impl.sloth;

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
 * King (Sloth) ability — Spirit Spear.
 *
 * <p>King hurls Chastiefol as a spirit spear in his look direction, dealing
 * damage to any enemy in the targeted cone in front of him.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#SPIRIT_SPEAR}</li>
 *   <li>Mana cost: 25</li>
 *   <li>Cooldown: 100 ticks (5 s)</li>
 *   <li>Damage: 10</li>
 * </ul>
 */
public class KingSpiritSpearAbility extends Ability {

    private static final double RANGE = 8.0;
    private static final double BOX_HALF_WIDTH = 1.0;
    private static final float DAMAGE = 10.0f;

    public KingSpiritSpearAbility() {
        super(AbilityType.SPIRIT_SPEAR, 25, 100);
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
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.SPIRIT_SPEAR, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
        }

        // Spawn particles along the spear path
        ServerLevel serverLevel = (ServerLevel) player.level();
        for (int i = 1; i <= 6; i++) {
            Vec3 pos = eyes.add(look.scale(i * (RANGE / 6.0)));
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANTED_HIT,
                    pos.x, pos.y, pos.z,
                    4,
                    0.1, 0.1, 0.1,
                    0.05
            );
        }

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS,
                1.0f,
                1.2f
        );
    }
}
