package com.sevensins.ability.impl.wrath;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
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
 * Meliodas (Wrath) ability — Hell Blaze.
 *
 * <p>Blasts dark fire at an enemy directly in front of the player, dealing
 * damage, igniting the target, spawning flame particles, and playing a
 * fire sound.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#HELL_BLAZE}</li>
 *   <li>Mana cost: 20</li>
 *   <li>Cooldown: 100 ticks (5 s)</li>
 *   <li>Damage: 8</li>
 * </ul>
 */
public class HellBlazeAbility extends Ability {

    private static final float DAMAGE = 8.0f;
    /** How far in front of the player to search for targets (blocks). */
    private static final double RANGE = 4.0;
    /** Half-width of the targeting box perpendicular to the look direction. */
    private static final double BOX_HALF_WIDTH = 1.5;
    /** Fire duration applied to hit targets (in seconds). */
    private static final int FIRE_DURATION_SECONDS = 3;

    public HellBlazeAbility() {
        super(AbilityType.HELL_BLAZE, 20, 100);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        Vec3 eyes = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // Build an AABB that covers the area in front of the player
        Vec3 end = eyes.add(look.scale(RANGE));
        AABB searchBox = new AABB(eyes, end).inflate(BOX_HALF_WIDTH);

        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity instanceof Enemy && entity != player
        );

        DamageSource source = player.damageSources().playerAttack(player);

        for (LivingEntity target : targets) {
            target.hurt(source, DAMAGE);
            target.setSecondsOnFire(FIRE_DURATION_SECONDS);
        }

        // Spawn flame particles along the look vector (server → all nearby clients)
        ServerLevel serverLevel = (ServerLevel) player.level();
        for (int i = 1; i <= 4; i++) {
            Vec3 pos = eyes.add(look.scale(i));
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    6,          // count
                    0.2, 0.2, 0.2, // spread
                    0.05        // speed
            );
        }

        // Play fire sound
        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS,
                1.0f,
                0.9f
        );
    }
}
