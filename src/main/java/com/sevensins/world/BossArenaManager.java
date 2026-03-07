package com.sevensins.world;

import com.sevensins.entity.DemonKingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages the final-boss arena state for the Demon King encounter.
 *
 * <p>Version 1 responsibilities:
 * <ul>
 *   <li>Tracks the active Demon King arena by boss UUID.</li>
 *   <li>Provides a simple bounds check so other systems can verify whether a
 *       player is inside the arena.</li>
 *   <li>Cleans up arena state when the encounter ends (boss dies or is
 *       unregistered).</li>
 * </ul>
 * </p>
 *
 * <p>All methods are null-safe and will not throw when encounter data is
 * missing — failsafe design ensures the boss fight continues even if arena
 * tracking fails.</p>
 *
 * <p>Singleton — obtain via {@link #getInstance()}.</p>
 */
public final class BossArenaManager {

    private static final BossArenaManager INSTANCE = new BossArenaManager();

    /**
     * Half-extent (blocks) of the square arena centred on the boss spawn point.
     * Effectively a 200 × 200 block arena.
     */
    public static final double DEMON_KING_ARENA_HALF_SIZE = 100.0;

    /** Active arenas keyed by boss entity UUID. */
    private final Map<UUID, ArenaInfo> activeArenas = new ConcurrentHashMap<>();

    private BossArenaManager() {}

    /** Returns the singleton {@link BossArenaManager}. */
    public static BossArenaManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Arena lifecycle
    // -------------------------------------------------------------------------

    /**
     * Registers a new Demon King arena centred on the spawn coordinates.
     * Safe to call more than once for the same UUID — replaces the previous entry.
     *
     * @param bossUUID boss entity UUID
     * @param centerX  X coordinate of the arena centre
     * @param centerY  Y coordinate of the arena centre (floor level)
     * @param centerZ  Z coordinate of the arena centre
     */
    public void openDemonKingArena(UUID bossUUID, double centerX, double centerY, double centerZ) {
        if (bossUUID == null) return;
        AABB bounds = new AABB(
                centerX - DEMON_KING_ARENA_HALF_SIZE, centerY - 10,
                centerZ - DEMON_KING_ARENA_HALF_SIZE,
                centerX + DEMON_KING_ARENA_HALF_SIZE, centerY + 50,
                centerZ + DEMON_KING_ARENA_HALF_SIZE);
        activeArenas.put(bossUUID, new ArenaInfo(bossUUID, bounds));
    }

    /**
     * Removes the arena associated with {@code bossUUID}.
     * Safe to call even if the arena was never registered.
     */
    public void closeArena(UUID bossUUID) {
        if (bossUUID == null) return;
        activeArenas.remove(bossUUID);
    }

    /**
     * Returns {@code true} if {@code player} is currently inside any active
     * Demon King arena.
     */
    public boolean isPlayerInArena(ServerPlayer player) {
        if (player == null) return false;
        for (ArenaInfo arena : activeArenas.values()) {
            if (arena.bounds().contains(player.getX(), player.getY(), player.getZ())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the {@link ArenaInfo} for the given boss UUID, or {@code null}
     * if no arena is registered.
     */
    @Nullable
    public ArenaInfo getArena(UUID bossUUID) {
        if (bossUUID == null) return null;
        return activeArenas.get(bossUUID);
    }

    /** Returns {@code true} if an arena is registered for {@code bossUUID}. */
    public boolean hasArena(UUID bossUUID) {
        return bossUUID != null && activeArenas.containsKey(bossUUID);
    }

    // -------------------------------------------------------------------------
    // ArenaInfo record
    // -------------------------------------------------------------------------

    /**
     * Immutable snapshot of a boss arena.
     *
     * @param bossUUID the UUID of the boss that owns this arena
     * @param bounds   the axis-aligned bounding box of the arena
     */
    public record ArenaInfo(UUID bossUUID, AABB bounds) {}
}
