package com.sevensins.ability.impl;

import com.sevensins.ability.Ability;
import com.sevensins.ability.AbilityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Meliodas ability — Full Counter.
 *
 * <p>Activating the ability marks the player as being in "counter state" for
 * 3 seconds (60 ticks).  While active, any incoming melee or ranged damage is
 * reflected back to the attacker at 1.5× the original amount and the counter
 * state is consumed.  Reflection is handled by
 * {@link com.sevensins.event.PassiveAbilityEvents#onFullCounterHurt}.
 *
 * <ul>
 *   <li>Type: {@link AbilityType#FULL_COUNTER}</li>
 *   <li>Mana cost: 20</li>
 *   <li>Cooldown: 200 ticks (~10 s)</li>
 * </ul>
 */
public class MeliodasFullCounterAbility extends Ability {

    /** NBT key used to store the counter-state flag in persistent player data. */
    public static final String NBT_ACTIVE = "seven_sins:full_counter_active";

    /** NBT key storing the game-tick at which the counter state expires. */
    public static final String NBT_EXPIRE = "seven_sins:full_counter_expire";

    /** Duration of the counter state in ticks (3 seconds). */
    private static final int DURATION_TICKS = 60;

    public MeliodasFullCounterAbility() {
        super(AbilityType.FULL_COUNTER, 20, 200);
    }

    @Override
    protected void execute(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        long expireTime = player.level().getGameTime() + DURATION_TICKS;

        CompoundTag data = player.getPersistentData();
        data.putBoolean(NBT_ACTIVE, true);
        data.putLong(NBT_EXPIRE, expireTime);
        // Expiry is checked lazily in isCounterActive().
        // Damage reflection is handled in PassiveAbilityEvents.onFullCounterHurt().
    }

    /**
     * Utility: returns whether {@code player} is currently in counter state.
     * Should be queried server-side only.
     */
    public static boolean isCounterActive(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.getBoolean(NBT_ACTIVE)) {
            return false;
        }
        if (player.level().getGameTime() >= data.getLong(NBT_EXPIRE)) {
            data.putBoolean(NBT_ACTIVE, false);
            return false;
        }
        return true;
    }

    /**
     * Deactivates the counter state for {@code player}.
     * Should be called server-side when the counter is consumed or expires early.
     */
    public static void deactivateCounter(Player player) {
        player.getPersistentData().putBoolean(NBT_ACTIVE, false);
    }
}
