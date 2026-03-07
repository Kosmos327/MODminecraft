package com.sevensins.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks active boss encounters.
 *
 * <p>The server side maintains a {@link ConcurrentHashMap} of active bosses keyed
 * by entity UUID.  The client side maintains a single nullable {@link BossInfo}
 * that is populated by {@link com.sevensins.network.packet.SyncBossStatePacket}
 * and consumed by {@link com.sevensins.client.hud.BossHealthOverlay}.</p>
 *
 * <p>All methods are null-safe and will not throw on missing data.</p>
 */
public final class BossManager {

    private static final BossManager INSTANCE = new BossManager();

    // Server-side: UUID → BossInfo for every active boss
    private final Map<UUID, BossInfo> activeBosses = new ConcurrentHashMap<>();

    // Server-side: boss UUID → set of tracked minion UUIDs
    private final Map<UUID, Set<UUID>> bossMinions = new ConcurrentHashMap<>();

    // Client-side: most recent synced boss state (null = no active boss)
    @Nullable
    private static volatile BossInfo clientBossState = null;

    private BossManager() {}

    /** Returns the singleton {@link BossManager}. */
    public static BossManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Server-side methods
    // -------------------------------------------------------------------------

    /**
     * Registers a new active boss.
     *
     * @param uuid      entity UUID of the boss
     * @param name      display name (e.g. "Red Demon")
     * @param currentHp current health
     * @param maxHp     maximum health
     */
    public void registerBoss(UUID uuid, String name, float currentHp, float maxHp) {
        if (uuid == null) return;
        activeBosses.put(uuid, new BossInfo(name, currentHp, maxHp, BossPhase.PHASE_1));
    }

    /**
     * Updates health and phase for an existing boss entry.
     * A no-op if the boss is not currently registered.
     */
    public void updateBoss(UUID uuid, float currentHp, BossPhase phase) {
        if (uuid == null) return;
        BossInfo existing = activeBosses.get(uuid);
        if (existing != null) {
            activeBosses.put(uuid, new BossInfo(existing.name(), currentHp, existing.maxHp(), phase));
        }
    }

    /**
     * Removes the boss from the active-boss registry and cleans up any tracked
     * minions.  The caller is responsible for despawning the minions in world;
     * use {@link #cleanupMinions(UUID, ServerLevel)} before or after this call.
     * Safe to call even if the boss was never registered.
     */
    public void unregisterBoss(UUID uuid) {
        if (uuid == null) return;
        activeBosses.remove(uuid);
        bossMinions.remove(uuid);
    }

    /** Returns {@code true} if a boss with the given UUID is currently tracked. */
    public boolean isActiveBoss(UUID uuid) {
        return uuid != null && activeBosses.containsKey(uuid);
    }

    // -------------------------------------------------------------------------
    // Minion tracking
    // -------------------------------------------------------------------------

    /**
     * Registers a minion as owned by the given boss.
     * A no-op if either UUID is {@code null}.
     *
     * @param bossUUID   UUID of the owning boss
     * @param minionUUID UUID of the spawned minion
     */
    public void registerMinion(UUID bossUUID, UUID minionUUID) {
        if (bossUUID == null || minionUUID == null) return;
        bossMinions.computeIfAbsent(bossUUID, k -> ConcurrentHashMap.newKeySet())
                   .add(minionUUID);
    }

    /**
     * Removes a specific minion from the boss's tracked minion set.
     * A no-op if either UUID is {@code null} or not tracked.
     *
     * @param bossUUID   UUID of the owning boss
     * @param minionUUID UUID of the minion to remove
     */
    public void unregisterMinion(UUID bossUUID, UUID minionUUID) {
        if (bossUUID == null || minionUUID == null) return;
        Set<UUID> minions = bossMinions.get(bossUUID);
        if (minions != null) minions.remove(minionUUID);
    }

    /**
     * Returns the number of currently tracked minions for the given boss.
     * Returns {@code 0} if the boss has no tracked minions or is not registered.
     *
     * @param bossUUID UUID of the boss
     */
    public int getActiveMinionCount(UUID bossUUID) {
        if (bossUUID == null) return 0;
        Set<UUID> minions = bossMinions.get(bossUUID);
        return minions == null ? 0 : minions.size();
    }

    /**
     * Returns an unmodifiable snapshot of the tracked minion UUIDs for the
     * given boss.  Returns an empty set if the boss has no tracked minions.
     *
     * @param bossUUID UUID of the boss
     */
    public Set<UUID> getTrackedMinions(UUID bossUUID) {
        if (bossUUID == null) return Collections.emptySet();
        Set<UUID> minions = bossMinions.get(bossUUID);
        return minions == null ? Collections.emptySet() : Collections.unmodifiableSet(minions);
    }

    /**
     * Despawns all tracked minions for the given boss and clears the tracking
     * set.  Minions that are no longer in the world are silently skipped.
     *
     * <p>This must be called from the server thread.</p>
     *
     * @param bossUUID UUID of the boss whose minions should be removed
     * @param level    the {@link ServerLevel} to search for minions
     */
    public void cleanupMinions(UUID bossUUID, ServerLevel level) {
        if (bossUUID == null || level == null) return;
        Set<UUID> minions = bossMinions.remove(bossUUID);
        if (minions == null) return;
        for (UUID minionUUID : minions) {
            try {
                Entity entity = level.getEntity(minionUUID);
                if (entity != null && entity.isAlive()) {
                    entity.discard();
                }
            } catch (Exception ignored) {
                // Safety: never crash if minion entity lookup fails
            }
        }
    }

    // -------------------------------------------------------------------------
    // Client-side methods
    // -------------------------------------------------------------------------

    /**
     * Updates the client-side boss state.
     * Pass {@code null} to signal that no boss is active (hides the overlay).
     */
    public static void setClientBossState(@Nullable BossInfo state) {
        clientBossState = state;
    }

    /**
     * Returns the most recently synced client-side boss state, or {@code null}
     * when no boss is active.
     */
    @Nullable
    public static BossInfo getClientBossState() {
        return clientBossState;
    }

    // -------------------------------------------------------------------------
    // BossInfo record
    // -------------------------------------------------------------------------

    /**
     * Immutable snapshot of a boss encounter's current state.
     *
     * @param name      display name shown in the overlay
     * @param currentHp current health
     * @param maxHp     maximum health
     * @param phase     active {@link BossPhase}
     */
    public record BossInfo(String name, float currentHp, float maxHp, BossPhase phase) {}
}
