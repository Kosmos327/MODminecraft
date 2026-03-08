package com.sevensins.ability.impl.greed;

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
 * Ban (Greed) ability — Fox Hunt.
 *
 * <p>A rapid dash-strike: Ban launches himself forward in his look direction,
 * teleporting a short distance and dealing damage to any enemy caught in his
 * path, representing his lightning-fast hunter mobility.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#FOX_HUNT}</li>
 *   <li>Mana cost: 30</li>
 *   <li>Cooldown: 80 ticks (4 s)</li>
 *   <li>Damage: 12</li>
 * </ul>
 */
public class BanFoxHuntAbility extends Ability {

    /** Distance of the dash in blocks. */
    private static final double DASH_DISTANCE = 5.0;
    /** Hit-box half-width around the dash path. */
    private static final double BOX_HALF_WIDTH = 1.2;
    private static final float DAMAGE = 12.0f;

    public BanFoxHuntAbility() {
        super(AbilityType.FOX_HUNT, 30, 80);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 origin = player.position();
        // Horizontal dash only — Y is preserved so the player stays at ground level
        Vec3 destination = origin.add(look.multiply(DASH_DISTANCE, 0.0, DASH_DISTANCE));

        // Collect enemies along the dash path before moving
        AABB dashBox = new AABB(origin, destination).inflate(BOX_HALF_WIDTH);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
                LivingEntity.class,
                dashBox,
                entity -> entity instanceof Enemy && entity != player
        );

        DamageSource source = player.damageSources().playerAttack(player);
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.FOX_HUNT, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
        }

        // Teleport the player to the destination
        player.teleportTo(destination.x, origin.y, destination.z);

        player.level().playSound(
                null,
                player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS,
                0.8f,
                1.4f
        );
    }
}
