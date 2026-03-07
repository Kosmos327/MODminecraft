package com.sevensins.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple server-side cooldown tracker keyed by player UUID and ability type.
 *
 * <p>Cooldowns are stored as the game-time (in milliseconds) at which the
 * cooldown expires.  {@link System#currentTimeMillis()} is used so that the
 * map stays correct even across server ticks.</p>
 */
public class CooldownManager {

    /** UUID → (AbilityType → expiry time in ms) */
    private static final Map<UUID, Map<AbilityType, Long>> COOLDOWNS = new HashMap<>();

    private CooldownManager() {}

    /**
     * Returns {@code true} when the player still has an active cooldown for
     * the given ability.
     */
    public static boolean isOnCooldown(UUID playerId, AbilityType ability) {
        Map<AbilityType, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null) return false;
        Long expiry = playerMap.get(ability);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    /**
     * Records a cooldown for {@code durationTicks} game-ticks (converted to
     * milliseconds at 50 ms/tick) starting from now.
     */
    public static void setCooldown(UUID playerId, AbilityType ability, int durationTicks) {
        COOLDOWNS
                .computeIfAbsent(playerId, id -> new HashMap<>())
                .put(ability, System.currentTimeMillis() + (long) durationTicks * 50L);
    }

    /**
     * Removes all cooldown entries for a player (e.g. on logout).
     */
    public static void clearCooldowns(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }
}
