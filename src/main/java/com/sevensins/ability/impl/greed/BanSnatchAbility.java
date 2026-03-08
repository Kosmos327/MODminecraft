package com.sevensins.ability.impl.greed;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import com.sevensins.config.BalanceHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Ban (Greed) ability — Snatch.
 *
 * <p>Short-range targeted strike that weakens nearby enemies and briefly
 * grants the player a strength boost, representing Ban's ability to steal
 * physical power from his opponents.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#SNATCH}</li>
 *   <li>Mana cost: 20</li>
 *   <li>Cooldown: 120 ticks (6 s)</li>
 *   <li>Damage: 6</li>
 * </ul>
 */
public class BanSnatchAbility extends Ability {

    private static final double RANGE = 3.0;
    private static final double BOX_HALF_WIDTH = 1.5;
    private static final float DAMAGE = 6.0f;
    /** Duration of the Weakness debuff on enemies (3 seconds). */
    private static final int DEBUFF_DURATION = 60;
    /** Duration of the Strength buff on the player (4 seconds). */
    private static final int BUFF_DURATION = 80;

    public BanSnatchAbility() {
        super(AbilityType.SNATCH, 20, 120);
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
        float effectiveDamage = BalanceHelper.getEffectiveAbilityDamage(player, AbilityType.SNATCH, DAMAGE);

        for (LivingEntity target : targets) {
            target.hurt(source, effectiveDamage);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION, 0, false, true));
        }

        // Grant the strength buff regardless of hits — the player paid mana/cooldown for the attempt
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION, 0, false, true));
    }
}
