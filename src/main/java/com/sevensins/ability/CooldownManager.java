package com.sevensins.ability;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side cooldown tracker keyed by player UUID and ability type.
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
     *
     * <p>Expired entries are silently removed during the lookup.</p>
     */
    public static boolean isOnCooldown(UUID playerId, AbilityType ability) {
        Map<AbilityType, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null) return false;
        Long expiry = playerMap.get(ability);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            playerMap.remove(ability);
            return false;
        }
        return true;
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
     * <p>Safe to call from any thread.  Expired entries are silently removed
     * during the lookup.</p>
     */
    public static long getRemainingMs(UUID playerId, AbilityType ability) {
        Map<AbilityType, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null) return 0L;
        Long expiry = playerMap.get(ability);
        if (expiry == null) return 0L;
        long remaining = expiry - System.currentTimeMillis();
        if (remaining <= 0) {
            playerMap.remove(ability);
            return 0L;
        }
        return remaining;
    }

    /**
     * Removes all expired cooldown entries for the given player.
     *
     * <p>Call periodically (e.g. on ability use or at a slow tick interval) to
     * reclaim memory for players with many stale entries.</p>
     *
     * @param playerId the UUID of the player to clean up
     */
    public static void removeExpiredForPlayer(UUID playerId) {
        Map<AbilityType, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null) return;
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<AbilityType, Long>> it = playerMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<AbilityType, Long> entry = it.next();
            if (entry.getValue() <= now) {
                it.remove();
            }
        }
        // Remove the outer entry too if it is now empty
        if (playerMap.isEmpty()) {
            COOLDOWNS.remove(playerId, playerMap);
        }
    }

    /**
     * Removes all cooldown entries for a player (e.g. on logout).
     */
    public static void clearCooldowns(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }
}
