package com.sevensins.world;

import com.sevensins.SevenSinsMod;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.story.StoryTriggerService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages all active dungeon runs on the server side.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Create and register new dungeon instances.</li>
 *   <li>Track dungeon state (active mobs, cleared flag).</li>
 *   <li>Detect dungeon clear when all tracked mobs are dead.</li>
 *   <li>Grant rewards and update quest/story on completion.</li>
 * </ul>
 *
 * <p>Singleton — obtain via {@link #getInstance()}.</p>
 *
 * <p>Thread-safe: the active-dungeon map uses {@link ConcurrentHashMap}.
 * All public methods are safe to call from the server tick thread.</p>
 */
public final class DungeonManager {

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static final DungeonManager INSTANCE = new DungeonManager();

    private DungeonManager() {}

    /** Returns the singleton {@link DungeonManager}. */
    public static DungeonManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Active dungeon registry
    // -------------------------------------------------------------------------

    private final Map<UUID, DungeonRun> activeRuns = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Demon Cave generation constants
    // -------------------------------------------------------------------------

    /** Width (X) of the generated cave room (odd number so the centre is clean). */
    private static final int CAVE_WIDTH  = 9;
    /** Depth (Z) of the generated cave room. */
    private static final int CAVE_DEPTH  = 13;
    /** Interior ceiling height (air blocks, not counting floor). */
    private static final int CAVE_HEIGHT = 4;
    /** Number of hostile mobs spawned inside the cave. */
    private static final int MOB_COUNT   = 6;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Creates a Demon Cave near {@code player} and registers the dungeon run.
     *
     * <p>The cave is dug into a flat area just in front of the player.
     * Stone walls, a stone floor, and a sealed ceiling surround the combat
     * area.  {@value MOB_COUNT} zombies are spawned inside and tracked.
     * When all tracked mobs die the dungeon is considered cleared.</p>
     *
     * <p>Fails silently if the player's level is {@code null} or not a
     * {@link ServerLevel}.</p>
     *
     * @param player the triggering player (owns the run and receives rewards)
     * @return the {@link UUID} assigned to this dungeon run, or {@code null}
     *         on failure
     */
    @Nullable
    public UUID spawnDemonCave(ServerPlayer player) {
        if (player == null) return null;
        if (!(player.level() instanceof ServerLevel level)) return null;

        try {
            // Place entrance two blocks in front of the player at their feet
            BlockPos origin = player.blockPosition()
                    .relative(player.getDirection(), 3);

            buildCaveRoom(level, origin);

            UUID dungeonId = UUID.randomUUID();
            DungeonRun run = new DungeonRun(dungeonId, DungeonType.DEMON_CAVE, origin, player.getUUID());
            activeRuns.put(dungeonId, run);

            spawnDungeonMobs(level, origin, dungeonId, run);

            // Set DEMON_CAVE_STARTED story flag on first entry
            StoryTriggerService.getInstance().onDungeonEntered(player, DungeonType.DEMON_CAVE);

            player.sendSystemMessage(Component.literal("Entered Demon Cave"));
            if (!run.getTrackedMobIds().isEmpty()) {
                player.sendSystemMessage(Component.literal(
                        "Enemies remaining: " + run.getTrackedMobIds().size()));
            }

            SevenSinsMod.LOGGER.info("[DungeonManager] Demon Cave {} created at {} for player {}",
                    dungeonId, origin, player.getName().getString());

            return dungeonId;

        } catch (Exception e) {
            SevenSinsMod.LOGGER.error("[DungeonManager] Failed to spawn Demon Cave: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Called when a tracked mob is killed.  Removes the mob from its run's
     * tracking set and, if the set becomes empty, marks the dungeon as cleared
     * and triggers rewards.
     *
     * @param mobId  UUID of the mob that just died
     * @param killer the player who killed the mob (may be {@code null})
     */
    public void onTrackedMobDied(UUID mobId, @Nullable ServerPlayer killer) {
        if (mobId == null) return;

        for (DungeonRun run : activeRuns.values()) {
            if (!run.getTrackedMobIds().contains(mobId)) continue;

            run.getTrackedMobIds().remove(mobId);

            // Notify killer about remaining count
            if (killer != null) {
                int remaining = run.getTrackedMobIds().size();
                if (remaining > 0) {
                    killer.sendSystemMessage(Component.literal(
                            "Enemies remaining: " + remaining));
                }
            }

            // All tracked mobs dead → dungeon cleared
            if (run.getTrackedMobIds().isEmpty() && !run.isCleared()) {
                clearDungeon(run, killer);
            }
            break;
        }
    }

    /**
     * Returns {@code true} if the given mob UUID is tracked by any active
     * dungeon run.
     */
    public boolean isTrackedMob(UUID mobId) {
        if (mobId == null) return false;
        for (DungeonRun run : activeRuns.values()) {
            if (run.getTrackedMobIds().contains(mobId)) return true;
        }
        return false;
    }

    /** Exposes all active runs (unmodifiable view) for inspection / testing. */
    public Map<UUID, DungeonRun> getActiveRuns() {
        return Collections.unmodifiableMap(activeRuns);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Carves a rectangular stone-walled cave room into the world at
     * {@code origin}.
     *
     * <p>Layout (from {@code origin}):
     * <ul>
     *   <li>Floor at Y = 0 (stone slabs replaced with smooth stone).</li>
     *   <li>Interior air: X=[−W/2, +W/2], Z=[0, D], Y=[1, HEIGHT].</li>
     *   <li>Ceiling/walls: solid stone brick layer.</li>
     * </ul>
     */
    private static void buildCaveRoom(ServerLevel level, BlockPos origin) {
        int halfW = CAVE_WIDTH / 2;
        BlockState stoneBrick = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState stone      = Blocks.STONE.defaultBlockState();
        BlockState air        = Blocks.AIR.defaultBlockState();

        for (int x = -halfW - 1; x <= halfW + 1; x++) {
            for (int z = -1; z <= CAVE_DEPTH + 1; z++) {
                for (int y = -1; y <= CAVE_HEIGHT + 1; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean isInterior = (x >= -halfW && x <= halfW)
                            && (z >= 0 && z < CAVE_DEPTH)
                            && (y >= 0 && y <= CAVE_HEIGHT);

                    if (isInterior) {
                        if (y == 0) {
                            level.setBlock(pos, stone, 3);
                        } else {
                            level.setBlock(pos, air, 3);
                        }
                    } else {
                        level.setBlock(pos, stoneBrick, 3);
                    }
                }
            }
        }
    }

    /**
     * Spawns {@value MOB_COUNT} zombies inside the cave room and registers
     * their UUIDs with the given {@link DungeonRun}.
     */
    private static void spawnDungeonMobs(ServerLevel level, BlockPos origin,
                                         UUID dungeonId, DungeonRun run) {
        int halfW = CAVE_WIDTH / 2;
        int spawnedCount = 0;

        for (int attempt = 0; attempt < MOB_COUNT * 3 && spawnedCount < MOB_COUNT; attempt++) {
            int offsetX = -halfW + 1 + ThreadLocalRandom.current().nextInt(CAVE_WIDTH - 2);
            int offsetZ = 1 + ThreadLocalRandom.current().nextInt(CAVE_DEPTH - 2);
            BlockPos spawnPos = origin.offset(offsetX, 1, offsetZ);

            Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
            zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    0, 0);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.COMMAND, null, null);

            if (level.addFreshEntity(zombie)) {
                run.getTrackedMobIds().add(zombie.getUUID());
                spawnedCount++;
            }
        }

        SevenSinsMod.LOGGER.debug("[DungeonManager] Spawned {} mobs for dungeon {}",
                spawnedCount, dungeonId);
    }

    /** Marks the dungeon as cleared and triggers all downstream effects. */
    private void clearDungeon(DungeonRun run, @Nullable ServerPlayer killer) {
        run.setCleared(true);
        activeRuns.remove(run.getDungeonId());

        // Resolve the reward target: prefer the killer, fall back to the owner
        ServerPlayer rewardTarget = killer;
        if (rewardTarget == null && run.getOwnerUUID() != null) {
            // owner resolution requires a server reference — skip gracefully
            SevenSinsMod.LOGGER.debug(
                    "[DungeonManager] No killer found; owner UUID={}", run.getOwnerUUID());
        }

        // Grant reward
        DungeonRewardTable.grantDemonCaveReward(rewardTarget);

        // Notify player
        if (rewardTarget != null) {
            rewardTarget.sendSystemMessage(
                    Component.literal("Demon Cave Cleared!"));

            // Quest completion
            QuestManager.completeDungeonQuest(rewardTarget, QuestRegistry.CLEAR_DEMON_CAVE_ID);

            // Story trigger
            StoryTriggerService.getInstance().onDungeonCleared(rewardTarget,
                    DungeonType.DEMON_CAVE);
        }

        SevenSinsMod.LOGGER.info("[DungeonManager] Dungeon {} ({}) cleared by {}",
                run.getDungeonId(), run.getType().getDisplayName(),
                rewardTarget != null ? rewardTarget.getName().getString() : "unknown");
    }

    // -------------------------------------------------------------------------
    // DungeonRun record
    // -------------------------------------------------------------------------

    /**
     * Mutable state for a single active dungeon run.
     */
    public static final class DungeonRun {

        private final UUID dungeonId;
        private final DungeonType type;
        private final BlockPos origin;
        @Nullable
        private final UUID ownerUUID;
        private final Set<UUID> trackedMobIds = new HashSet<>();
        private boolean cleared = false;

        DungeonRun(UUID dungeonId, DungeonType type, BlockPos origin,
                   @Nullable UUID ownerUUID) {
            this.dungeonId  = dungeonId;
            this.type       = type;
            this.origin     = origin;
            this.ownerUUID  = ownerUUID;
        }

        public UUID getDungeonId()           { return dungeonId; }
        public DungeonType getType()         { return type; }
        public BlockPos getOrigin()          { return origin; }
        @Nullable
        public UUID getOwnerUUID()           { return ownerUUID; }
        /** Mutable set of tracked mob UUIDs (alive or pending death). */
        public Set<UUID> getTrackedMobIds()  { return trackedMobIds; }
        public boolean isCleared()           { return cleared; }
        void setCleared(boolean cleared)     { this.cleared = cleared; }
    }
}
