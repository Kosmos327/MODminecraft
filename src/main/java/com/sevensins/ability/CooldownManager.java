package com.sevensins.ability;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple server-side cooldown tracker keyed by player UUID and ability type.
 *
 * <p>Cooldowns are stored as the game-time (in milliseconds) at which the
 * cooldown expires.  {@link System#currentTimeMillis()} is used so that the
 * map stays correct even across server ticks.</p>
 *
 * <p>Thread-safe: uses {@link ConcurrentHashMap} for both the outer UUID map
 * and each inner ability map, so concurrent reads from the server tick thread
 * and network-handler threads do not cause {@link java.util.ConcurrentModificationException}.</p>
 */
public class CooldownManager {

    /** UUID → (AbilityType → expiry time in ms) */
    private static final Map<UUID, Map<AbilityType, Long>> COOLDOWNS = new ConcurrentHashMap<>();

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
                .computeIfAbsent(playerId, id -> new ConcurrentHashMap<>())
                .put(ability, System.currentTimeMillis() + (long) durationTicks * 50L);
    }

    /**
     * Returns the remaining cooldown time in milliseconds for the given ability,
     * or {@code 0} if the ability is not on cooldown.
     *
     * <p>Safe to call from any thread. Returns 0 when no cooldown entry exists.</p>
     */
    public static long getRemainingMs(UUID playerId, AbilityType ability) {
        Map<AbilityType, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null) return 0L;
        Long expiry = playerMap.get(ability);
        if (expiry == null) return 0L;
        return Math.max(0L, expiry - System.currentTimeMillis());
    }

    /**
     * Removes all cooldown entries for a player (e.g. on logout).
     */
    public static void clearCooldowns(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }
}
