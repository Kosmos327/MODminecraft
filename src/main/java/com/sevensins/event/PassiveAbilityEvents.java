package com.sevensins.event;

import com.sevensins.SevenSinsMod;
import com.sevensins.ability.PassiveAbilityManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge event subscriber that applies passive ability effects in combat and on tick.
 *
 * <p>Hooks handled here:</p>
 * <ul>
 *   <li>{@link LivingHurtEvent} (NORMAL priority) – attacker bonus damage and
 *       GLUTTONY damage reduction for the defender.</li>
 *   <li>{@link LivingHurtEvent} (LOW priority) – GREED lifesteal and LUST debuff
 *       application (runs after the damage amount is finalised by higher-priority handlers).</li>
 *   <li>{@link TickEvent.PlayerTickEvent} – periodic passive tick effects.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID)
public final class PassiveAbilityEvents {

    private PassiveAbilityEvents() {}

    // -------------------------------------------------------------------------
    // Damage modification – attacker and defender passives
    // -------------------------------------------------------------------------

    /**
     * Applies attacker bonus damage (WRATH, PRIDE) and defender damage reduction (GLUTTONY).
     * Also reduces outgoing damage when the attacker is under a LUST debuff.
     *
     * <p>Runs at {@link EventPriority#NORMAL} so that base damage is already set.</p>
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity defender = event.getEntity();
        float amount = event.getAmount();

        if (amount <= 0f) return;

        // --- Attacker bonus damage ---
        if (event.getSource().getEntity() instanceof ServerPlayer attacker) {
            float bonusDamage = PassiveAbilityManager.getBonusDamage(attacker);
            if (bonusDamage > 0f) {
                amount *= (1f + bonusDamage);
            }
        }

        // --- Lust debuff: the striking entity deals less damage if debuffed ---
        if (event.getSource().getEntity() instanceof LivingEntity attackingEntity
                && !(attackingEntity instanceof ServerPlayer)
                && PassiveAbilityManager.isLustDebuffed(attackingEntity)) {
            amount *= (1f - PassiveAbilityManager.LUST_DEBUFF_REDUCTION);
        }

        // --- Defender resistance (GLUTTONY / MERLIN) ---
        if (defender instanceof ServerPlayer defenderPlayer) {
            float resistance = PassiveAbilityManager.getResistanceBonus(defenderPlayer);
            if (resistance > 0f) {
                amount *= (1f - resistance);
            }
        }

        event.setAmount(Math.max(0f, amount));
    }

    /**
     * Applies post-damage passive effects: GREED lifesteal and LUST debuff application.
     *
     * <p>Runs at {@link EventPriority#LOW} so that the final damage amount is used.</p>
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurtPostDamage(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer attacker)) return;

        LivingEntity target = event.getEntity();
        float finalAmount = event.getAmount();

        PassiveAbilityManager.onDamageDealt(attacker, target, finalAmount);
    }

    // -------------------------------------------------------------------------
    // Player tick – periodic passive effects
    // -------------------------------------------------------------------------

    /**
     * Delegates per-tick passive logic to {@link PassiveAbilityManager#onPlayerTick}.
     * Only runs server-side at the end of the tick phase.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        PassiveAbilityManager.onPlayerTick(serverPlayer);
    }
}
