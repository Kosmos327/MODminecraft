package com.sevensins.network;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight dirty-flag and sync-throttle helper for server → client packets.
 *
 * <h2>Purpose</h2>
 * Many systems (sin data, character data, cooldown HUD) previously sent sync
 * packets on every relevant event regardless of whether the underlying value
 * had actually changed, or sent the same packet multiple times in quick
 * succession.  This helper provides two complementary tools to reduce that
 * overhead without removing any correctness guarantees:
 *
 * <h3>Dirty-flag tracking</h3>
 * Mark a player as dirty when their data changes.  Callers can then test the
 * flag before sending a sync packet and clear it once the packet is sent.
 *
 * <h3>Sync throttling</h3>
 * Record the last time a sync was sent for a player and skip the send when
 * less than {@code minimumIntervalMs} milliseconds have passed.  Useful for
 * frequently-updating values (e.g. sin data syncs after dimension changes that
 * fire in rapid succession).
 *
 * <h2>Thread safety</h2>
 * All internal maps use {@link ConcurrentHashMap}; individual read-modify-write
 * sequences are <em>not</em> atomic, but the worst outcome is an extra packet
 * being sent – never data loss.
 */
public final class DirtySyncTracker {

    // -------------------------------------------------------------------------
    // Dirty flags (per-player UUID)
    // -------------------------------------------------------------------------

    /** Players whose data has changed and requires a sync packet. */
    private static final Map<UUID, Boolean> DIRTY_FLAGS = new ConcurrentHashMap<>();

    /**
     * Marks the player as dirty – their data has changed and a sync packet
     * should be sent soon.
     *
     * @param playerId the UUID of the player
     */
    public static void markDirty(UUID playerId) {
        if (playerId != null) {
            DIRTY_FLAGS.put(playerId, Boolean.TRUE);
        }
    }

    /**
     * Returns {@code true} if the player has been marked dirty since the last
     * call to {@link #clearDirty(UUID)}.
     *
     * @param playerId the UUID of the player
     * @return {@code true} if a sync packet should be sent
     */
    public static boolean isDirty(UUID playerId) {
        return playerId != null && Boolean.TRUE.equals(DIRTY_FLAGS.get(playerId));
    }

    /**
     * Clears the dirty flag for the player (call after successfully sending
     * the sync packet).
     *
     * @param playerId the UUID of the player
     */
    public static void clearDirty(UUID playerId) {
        if (playerId != null) {
            DIRTY_FLAGS.remove(playerId);
        }
    }

    // -------------------------------------------------------------------------
    // Sync throttle (per-player UUID)
    // -------------------------------------------------------------------------

    /** Players → last sync timestamp in milliseconds. */
    private static final Map<UUID, Long> LAST_SYNC_TIMES = new ConcurrentHashMap<>();

    /**
     * Returns {@code true} when enough time has elapsed since the last sync
     * for this player to justify sending another packet.
     *
     * <p>A player that has never been synced always returns {@code true}.</p>
     *
     * @param playerId          the UUID of the player
     * @param minimumIntervalMs minimum milliseconds between syncs
     * @return {@code true} if the player may be synced again
     */
    public static boolean canSync(UUID playerId, long minimumIntervalMs) {
        if (playerId == null) return false;
        Long last = LAST_SYNC_TIMES.get(playerId);
        return last == null || (System.currentTimeMillis() - last) >= minimumIntervalMs;
    }

    /**
     * Records that a sync packet was just sent for the player.
     * Call immediately after dispatching the packet.
     *
     * @param playerId the UUID of the player
     */
    public static void recordSync(UUID playerId) {
        if (playerId != null) {
            LAST_SYNC_TIMES.put(playerId, System.currentTimeMillis());
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle cleanup
    // -------------------------------------------------------------------------

    /**
     * Removes all tracking state for the given player.
     * Should be called when the player logs out to prevent stale entries.
     *
     * @param playerId the UUID of the player
     */
    public static void removePlayer(UUID playerId) {
        if (playerId != null) {
            DIRTY_FLAGS.remove(playerId);
            LAST_SYNC_TIMES.remove(playerId);
        }
    }

    // -------------------------------------------------------------------------

    private DirtySyncTracker() {}
}
