package com.sevensins.boss;

import javax.annotation.Nullable;
import java.util.Map;
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
     * Removes the boss from the active-boss registry.
     * Safe to call even if the boss was never registered.
     */
    public void unregisterBoss(UUID uuid) {
        if (uuid == null) return;
        activeBosses.remove(uuid);
    }

    /** Returns {@code true} if a boss with the given UUID is currently tracked. */
    public boolean isActiveBoss(UUID uuid) {
        return uuid != null && activeBosses.containsKey(uuid);
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
