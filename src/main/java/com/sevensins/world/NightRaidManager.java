package com.sevensins.world;

import com.sevensins.SevenSinsMod;
import com.sevensins.entity.MythicRedDemonEntity;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.registry.ModEntities;
import com.sevensins.story.StoryFlag;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages all active Night Demon Raid encounters on the server side.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Start a raid for a player.</li>
 *   <li>Track active raid state (current wave, remaining enemies).</li>
 *   <li>Advance waves when the current wave is cleared.</li>
 *   <li>Finish the raid and grant rewards when all waves are cleared.</li>
 *   <li>Fail the raid safely if needed.</li>
 * </ul>
 *
 * <h2>Version-1 raid flow</h2>
 * <ol>
 *   <li>Wave 1: {@value #WAVE1_SPAWN_COUNT} zombies (normal mobs).</li>
 *   <li>Wave 2: {@value #WAVE2_SPAWN_COUNT} zombies (normal mobs).</li>
 *   <li>Wave 3: boss wave — one Mythic Red Demon.</li>
 * </ol>
 *
 * <p>Singleton — obtain via {@link #getInstance()}.</p>
 *
 * <p>Thread-safe: the active-raid map uses {@link ConcurrentHashMap}.
 * All public methods are safe to call from the server tick thread.</p>
 */
public final class NightRaidManager {

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    private static final NightRaidManager INSTANCE = new NightRaidManager();

    private NightRaidManager() {}

    /** Returns the singleton {@link NightRaidManager}. */
    public static NightRaidManager getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Wave definitions
    // -------------------------------------------------------------------------

    /** Mob count for wave 1. */
    private static final int WAVE1_SPAWN_COUNT = 4;

    /** Mob count for wave 2. */
    private static final int WAVE2_SPAWN_COUNT = 6;

    /** Predefined raid waves: 3 total, last is a boss wave. */
    private static final List<RaidWave> RAID_WAVES = List.of(
            new RaidWave(1, WAVE1_SPAWN_COUNT, false),
            new RaidWave(2, WAVE2_SPAWN_COUNT, false),
            new RaidWave(3, 1, true)
    );

    // -------------------------------------------------------------------------
    // Spawn constants
    // -------------------------------------------------------------------------

    /** Radius around the player in which raid mobs are spawned. */
    private static final int SPAWN_RADIUS = 8;

    // -------------------------------------------------------------------------
    // Active raid registry
    // -------------------------------------------------------------------------

    /** Active raids, keyed by owner (player) UUID. */
    private final Map<UUID, RaidRun> activeRaids = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Starts a Night Demon Raid for {@code player}.
     *
     * <p>If the player already has an active raid this method is a no-op
     * and returns {@code false}.</p>
     *
     * <p>Optionally requires nighttime ({@code requireNight}).  If the world
     * is not night the player is notified and the raid is not started.</p>
     *
     * @param player       the player starting the raid
     * @param requireNight {@code true} to enforce a nighttime restriction
     * @return {@code true} if the raid was successfully started
     */
    public boolean startRaid(ServerPlayer player, boolean requireNight) {
        if (player == null) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        if (activeRaids.containsKey(player.getUUID())) {
            player.sendSystemMessage(Component.literal("A raid is already in progress."));
            return false;
        }

        if (requireNight && !isNight(level)) {
            player.sendSystemMessage(
                    Component.literal("Night Demon Raids can only begin at night."));
            return false;
        }

        RaidRun run = new RaidRun(player.getUUID(), player.blockPosition());
        activeRaids.put(player.getUUID(), run);

        player.sendSystemMessage(Component.literal("Night Raid started!"));
        SevenSinsMod.LOGGER.info("[NightRaidManager] Raid started for player {}",
                player.getName().getString());

        // Start first wave
        startWave(run, player, level);
        return true;
    }

    /**
     * Called when a tracked raid mob dies.  Updates wave tracking and advances
     * to the next wave or completes the raid when appropriate.
     *
     * @param mobId  UUID of the mob that just died
     * @param killer the player who killed the mob (may be {@code null})
     */
    public void onTrackedMobDied(UUID mobId, @Nullable ServerPlayer killer) {
        if (mobId == null) return;

        for (RaidRun run : activeRaids.values()) {
            if (!run.getTrackedMobIds().contains(mobId)) continue;

            run.getTrackedMobIds().remove(mobId);

            if (!run.getTrackedMobIds().isEmpty()) {
                break; // still mobs alive in this wave
            }

            // Wave cleared — find the owner and advance
            ServerPlayer owner = resolveOwner(run, killer);
            if (run.getCurrentWaveIndex() >= RAID_WAVES.size() - 1) {
                // Final wave cleared → raid complete; give reward to owner
                completeRaid(run, owner);
            } else {
                // Advance to next wave
                run.setCurrentWaveIndex(run.getCurrentWaveIndex() + 1);
                if (owner != null && owner.level() instanceof ServerLevel lvl) {
                    startWave(run, owner, lvl);
                } else if (killer != null && killer.level() instanceof ServerLevel killerLevel) {
                    // Fallback: use killer's level if owner couldn't be resolved
                    startWave(run, killer, killerLevel);
                } else {
                    SevenSinsMod.LOGGER.warn(
                            "[NightRaidManager] Could not advance wave: owner not on server");
                    activeRaids.remove(run.getOwnerUUID());
                }
            }
            break;
        }
    }

    /**
     * Returns {@code true} if the given mob UUID is currently tracked by an
     * active raid run.
     */
    public boolean isTrackedMob(UUID mobId) {
        if (mobId == null) return false;
        for (RaidRun run : activeRaids.values()) {
            if (run.getTrackedMobIds().contains(mobId)) return true;
        }
        return false;
    }

    /**
     * Fails the raid for {@code playerUUID} safely, removing all state.
     * Sends a message to the player if they are online.
     *
     * @param playerUUID UUID of the raid owner
     * @param player     the server player if available (may be {@code null})
     */
    public void failRaid(UUID playerUUID, @Nullable ServerPlayer player) {
        if (playerUUID == null) return;
        activeRaids.remove(playerUUID);
        if (player != null) {
            player.sendSystemMessage(Component.literal("Night Raid failed."));
        }
        SevenSinsMod.LOGGER.info("[NightRaidManager] Raid failed for player {}",
                playerUUID);
    }

    /** Exposes all active raids (unmodifiable view) for inspection / testing. */
    public Map<UUID, RaidRun> getActiveRaids() {
        return Collections.unmodifiableMap(activeRaids);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Starts the current wave for the given run, spawning the appropriate
     * enemies and notifying the player.
     */
    private void startWave(RaidRun run, ServerPlayer player, ServerLevel level) {
        RaidWave wave = RAID_WAVES.get(run.getCurrentWaveIndex());

        player.sendSystemMessage(
                Component.literal("Wave " + wave.getWaveNumber() + " begins!"));

        if (wave.isBossWave()) {
            player.sendSystemMessage(
                    Component.literal("A Mythic demon has been detected!"));
            spawnMythicBoss(level, run, player.blockPosition());
        } else {
            spawnWaveMobs(level, run, player.blockPosition(), wave.getSpawnCount());
        }

        // If no mobs were tracked (spawn completely failed), advance or complete immediately
        if (run.getTrackedMobIds().isEmpty()) {
            SevenSinsMod.LOGGER.warn(
                    "[NightRaidManager] Wave {} for player {} had no tracked mobs; advancing",
                    wave.getWaveNumber(), player.getName().getString());
            if (run.getCurrentWaveIndex() >= RAID_WAVES.size() - 1) {
                completeRaid(run, player);
            } else {
                run.setCurrentWaveIndex(run.getCurrentWaveIndex() + 1);
                startWave(run, player, level);
            }
            return;
        }

        SevenSinsMod.LOGGER.info("[NightRaidManager] Wave {} started for player {} ({} mobs tracked)",
                wave.getWaveNumber(), player.getName().getString(),
                run.getTrackedMobIds().size());
    }

    /** Spawns regular hostile mobs for a standard wave. */
    private static void spawnWaveMobs(ServerLevel level, RaidRun run,
                                       BlockPos center, int count) {
        int spawned = 0;
        for (int attempt = 0; attempt < count * 3 && spawned < count; attempt++) {
            BlockPos spawnPos = randomPosAround(center, SPAWN_RADIUS);
            Zombie zombie = new Zombie(EntityType.ZOMBIE, level);
            zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(),
                    spawnPos.getZ() + 0.5, 0, 0);
            zombie.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                    MobSpawnType.EVENT, null, null);
            if (level.addFreshEntity(zombie)) {
                run.getTrackedMobIds().add(zombie.getUUID());
                spawned++;
            }
        }
        if (spawned == 0) {
            SevenSinsMod.LOGGER.warn(
                    "[NightRaidManager] No mobs spawned for wave {}; wave will be auto-skipped",
                    RAID_WAVES.get(run.getCurrentWaveIndex()).getWaveNumber());
            // Caller (startWave) will detect trackedMobIds is empty and advance the wave
        }
    }

    /** Spawns the Mythic Red Demon for the boss wave. */
    private static void spawnMythicBoss(ServerLevel level, RaidRun run, BlockPos center) {
        BlockPos spawnPos = center.offset(3, 0, 3);
        MythicRedDemonEntity boss = new MythicRedDemonEntity(
                ModEntities.MYTHIC_RED_DEMON.get(), level);
        boss.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(),
                spawnPos.getZ() + 0.5, 0, 0);
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos),
                MobSpawnType.EVENT, null, null);
        if (level.addFreshEntity(boss)) {
            run.getTrackedMobIds().add(boss.getUUID());
        } else {
            // Fail: leave trackedMobIds empty so startWave() can detect the failure
            // and complete the raid with the reward rather than silently hanging.
            SevenSinsMod.LOGGER.warn(
                    "[NightRaidManager] Failed to spawn Mythic Red Demon for boss wave");
        }
    }

    /** Completes the raid, grants rewards, and updates quests / story flags. */
    private void completeRaid(RaidRun run, @Nullable ServerPlayer player) {
        run.setCompleted(true);
        activeRaids.remove(run.getOwnerUUID());

        if (player != null) {
            player.sendSystemMessage(
                    Component.literal("Night Raid complete! You have survived the darkness."));

            // Grant rewards
            RaidRewardTable.grantRaidReward(player);

            // Quest completion
            QuestManager.completeDungeonQuest(player, QuestRegistry.SURVIVE_NIGHT_RAID_ID);

            // Story flag
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData()
                            .addStoryFlag(StoryFlag.NIGHT_RAID_COMPLETE.getId()));
        }

        SevenSinsMod.LOGGER.info("[NightRaidManager] Raid completed for player {}",
                run.getOwnerUUID());
    }

    /**
     * Resolves the active owner player.
     *
     * <p>Looks up the owner by UUID via the killer's server reference (the
     * most reliable way to get a {@link net.minecraft.server.MinecraftServer}
     * without a separate reference).  Falls back to {@code killer} if the owner
     * cannot be found (e.g. they logged off during the raid).</p>
     */
    @Nullable
    private static ServerPlayer resolveOwner(RaidRun run, @Nullable ServerPlayer killer) {
        // Try to look up owner by UUID using the killer's server
        if (killer != null && killer.getServer() != null) {
            ServerPlayer owner = killer.getServer()
                    .getPlayerList()
                    .getPlayer(run.getOwnerUUID());
            if (owner != null) {
                return owner;
            }
        }
        // Owner not found; fall back to killer so progression is not completely lost
        return killer;
    }

    /** Returns {@code true} if the world time corresponds to night. */
    private static boolean isNight(ServerLevel level) {
        long time = level.getDayTime() % 24000L;
        return time >= 13000L && time <= 23000L;
    }

    /** Returns a random {@link BlockPos} within {@code radius} blocks of {@code center}. */
    private static BlockPos randomPosAround(BlockPos center, int radius) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int dx = rng.nextInt(-radius, radius + 1);
        int dz = rng.nextInt(-radius, radius + 1);
        return center.offset(dx, 0, dz);
    }

    // -------------------------------------------------------------------------
    // RaidRun record
    // -------------------------------------------------------------------------

    /**
     * Mutable state for a single active Night Demon Raid.
     */
    public static final class RaidRun {

        private final UUID ownerUUID;
        private final BlockPos origin;
        private final Set<UUID> trackedMobIds = new HashSet<>();
        private int currentWaveIndex = 0;
        private boolean completed = false;

        RaidRun(UUID ownerUUID, BlockPos origin) {
            this.ownerUUID = ownerUUID;
            this.origin    = origin;
        }

        public UUID getOwnerUUID()            { return ownerUUID; }
        public BlockPos getOrigin()           { return origin; }
        /** Mutable set of tracked mob UUIDs for the current wave. */
        public Set<UUID> getTrackedMobIds()   { return trackedMobIds; }
        public int getCurrentWaveIndex()      { return currentWaveIndex; }
        void setCurrentWaveIndex(int idx)     { this.currentWaveIndex = idx; }
        public boolean isCompleted()          { return completed; }
        void setCompleted(boolean completed)  { this.completed = completed; }
    }
}
