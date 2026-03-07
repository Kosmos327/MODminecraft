package com.sevensins.util;

import com.sevensins.SevenSinsMod;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.quest.PlayerQuestData;
import com.sevensins.story.StoryChapter;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

/**
 * Lightweight development helper for playtesting and release-candidate validation.
 *
 * <p>All methods in this class are no-ops in production (see {@link #ENABLED}).
 * They log concise state snapshots to the mod logger when enabled, helping
 * developers verify key system transitions during manual playtests.</p>
 *
 * <h2>Usage</h2>
 * <p>Enable by setting {@link #ENABLED} to {@code true} during development.
 * Disable (default: {@code false}) before shipping to avoid log spam.</p>
 *
 * <h2>Validation categories</h2>
 * <ul>
 *   <li>New-player flow (character selected, chapter initialised)</li>
 *   <li>Mid-game progression (level, skill points, ability unlock)</li>
 *   <li>Quest lifecycle (assigned, progress, completed)</li>
 *   <li>Story flag consistency</li>
 *   <li>Boss registration/unregistration</li>
 *   <li>Dungeon enter/clear</li>
 *   <li>Sacred treasure equipped</li>
 *   <li>Save/reload persistence</li>
 *   <li>Death/cleanup recovery</li>
 * </ul>
 */
public final class PlaytestHelper {

    /**
     * Master switch. Set to {@code true} during development to enable
     * diagnostic logging. Must be {@code false} for release builds.
     */
    public static final boolean ENABLED = false;

    private PlaytestHelper() {}

    // -------------------------------------------------------------------------
    // Character / progression checks
    // -------------------------------------------------------------------------

    /**
     * Logs a snapshot of the player's core RPG state.
     * Call after any significant progression event to verify consistency.
     */
    public static void logPlayerState(ServerPlayer player) {
        if (!ENABLED || player == null) return;
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            SevenSinsMod.LOGGER.info(
                    "[Playtest] Player={} | Char={} | Level={} | XP={} | Mana={}/{} | SP={} | Abilities={}",
                    player.getName().getString(),
                    data.getSelectedCharacter(),
                    data.getLevel(),
                    data.getExperience(),
                    data.getMana(),
                    data.getMaxMana(),
                    data.getSkillPoints(),
                    data.getUnlockedAbilities().size());
        });
    }

    /**
     * Validates that a player's character state is internally consistent.
     * Logs warnings for any detected anomalies without throwing.
     *
     * <p>Checks performed:
     * <ul>
     *   <li>Character != NONE &rarr; story stage should be &ge; AWAKENING</li>
     *   <li>Level &ge; 1</li>
     *   <li>Skill points &ge; 0</li>
     *   <li>Mana in [0, maxMana]</li>
     * </ul>
     * </p>
     */
    public static void validateCharacterState(ServerPlayer player) {
        if (!ENABLED || player == null) return;
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            String name = player.getName().getString();

            if (data.getSelectedCharacter() != CharacterType.NONE
                    && data.getPersonalStoryStage() < StoryChapter.AWAKENING.getStage()) {
                SevenSinsMod.LOGGER.warn(
                        "[Playtest] WARN: {} has character {} but storyStage={}",
                        name, data.getSelectedCharacter(), data.getPersonalStoryStage());
            }
            if (data.getLevel() < 1) {
                SevenSinsMod.LOGGER.warn("[Playtest] WARN: {} has invalid level={}", name, data.getLevel());
            }
            if (data.getSkillPoints() < 0) {
                SevenSinsMod.LOGGER.warn("[Playtest] WARN: {} has negative skillPoints={}", name, data.getSkillPoints());
            }
            if (data.getMana() < 0 || data.getMana() > data.getMaxMana()) {
                SevenSinsMod.LOGGER.warn("[Playtest] WARN: {} has invalid mana={}/{}", name, data.getMana(), data.getMaxMana());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Quest / story checks
    // -------------------------------------------------------------------------

    /**
     * Logs the player's current quest and story flag state.
     */
    public static void logQuestState(ServerPlayer player) {
        if (!ENABLED || player == null) return;
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData qd = cap.getData().getQuestData();
            SevenSinsMod.LOGGER.info(
                    "[Playtest] Quest | Player={} | Active='{}' | Completed={} | Flags={}",
                    player.getName().getString(),
                    qd.getActiveQuestId(),
                    qd.getCompletedQuestIds(),
                    qd.getStoryFlags());
        });
    }

    /**
     * Validates quest state for common consistency issues and logs warnings.
     *
     * <p>Checks performed:
     * <ul>
     *   <li>Active quest not in completed set</li>
     *   <li>No negative quest progress values</li>
     * </ul>
     * </p>
     */
    public static void validateQuestState(ServerPlayer player) {
        if (!ENABLED || player == null) return;
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData qd = cap.getData().getQuestData();
            String name = player.getName().getString();
            String active = qd.getActiveQuestId();

            if (!active.isEmpty() && qd.isCompleted(active)) {
                SevenSinsMod.LOGGER.warn(
                        "[Playtest] WARN: {} has active quest '{}' that is also in completed set",
                        name, active);
            }
            qd.getQuestProgress().forEach((questId, progress) -> {
                if (progress < 0) {
                    SevenSinsMod.LOGGER.warn(
                            "[Playtest] WARN: {} has negative progress={} for quest '{}'",
                            name, progress, questId);
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // Transition event loggers
    // -------------------------------------------------------------------------

    /** Log when a character is confirmed selected. */
    public static void onCharacterSelected(ServerPlayer player, CharacterType type) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] CharacterSelected: player={} char={}",
                player.getName().getString(), type);
    }

    /** Log when a quest is assigned. */
    public static void onQuestAssigned(ServerPlayer player, String questId) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] QuestAssigned: player={} quest={}",
                player.getName().getString(), questId);
    }

    /** Log when a quest is completed. */
    public static void onQuestCompleted(ServerPlayer player, String questId) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] QuestCompleted: player={} quest={}",
                player.getName().getString(), questId);
    }

    /** Log when a story flag is set. */
    public static void onStoryFlagSet(ServerPlayer player, String flag) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] StoryFlagSet: player={} flag={}",
                player.getName().getString(), flag);
    }

    /** Log when a boss is registered (encounter started). */
    public static void onBossRegistered(@Nullable String bossName) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] BossRegistered: {}", bossName);
    }

    /** Log when a boss is unregistered (encounter ended). */
    public static void onBossUnregistered(@Nullable String bossName) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] BossUnregistered: {}", bossName);
    }

    /** Log when a dungeon is entered. */
    public static void onDungeonEntered(ServerPlayer player, String dungeonType) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] DungeonEntered: player={} type={}",
                player.getName().getString(), dungeonType);
    }

    /** Log when a dungeon is cleared. */
    public static void onDungeonCleared(ServerPlayer player, String dungeonType) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] DungeonCleared: player={} type={}",
                player.getName().getString(), dungeonType);
    }

    /** Log when a player dies to verify cleanup occurs. */
    public static void onPlayerDeath(ServerPlayer player) {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("[Playtest] PlayerDeath: player={}", player.getName().getString());
        logPlayerState(player);
    }

    // -------------------------------------------------------------------------
    // Release-candidate checklist summary
    // -------------------------------------------------------------------------

    /**
     * Prints a brief release-candidate validation summary to the log.
     *
     * <p>Intended to be called once from a developer command or startup hook.
     * Lists the manual playtest steps that should be verified before a release.</p>
     */
    public static void printReleaseChecklist() {
        if (!ENABLED) return;
        SevenSinsMod.LOGGER.info("===== Seven Deadly Sins — Release Candidate Checklist =====");
        SevenSinsMod.LOGGER.info("[ ] 1. Start fresh world — character selection screen appears");
        SevenSinsMod.LOGGER.info("[ ] 2. Choose a sin — skill tree / mana / HUD initialise correctly");
        SevenSinsMod.LOGGER.info("[ ] 3. Gain XP and level up — sin level increases, skill point awarded");
        SevenSinsMod.LOGGER.info("[ ] 4. Unlock an ability — skill points deducted, ability appears in HUD");
        SevenSinsMod.LOGGER.info("[ ] 5. Use normal ability — mana consumed, cooldown shown in HUD");
        SevenSinsMod.LOGGER.info("[ ] 6. Activate ultimate — proper animation / effect fires");
        SevenSinsMod.LOGGER.info("[ ] 7. Complete first quest — story chapter advances, next quest assigned");
        SevenSinsMod.LOGGER.info("[ ] 8. Fight Red Demon — boss HP bar appears, phase 2 triggers at 50%");
        SevenSinsMod.LOGGER.info("[ ] 9. Clear Demon Cave — dungeon cleared message, quest completes");
        SevenSinsMod.LOGGER.info("[ ] 10. Upgrade a sacred treasure — stat bonus applies correctly");
        SevenSinsMod.LOGGER.info("[ ] 11. Relog / reload world — all data persists, no desync");
        SevenSinsMod.LOGGER.info("[ ] 12. Die during active system state — cooldowns cleared, no stale overlay");
        SevenSinsMod.LOGGER.info("[ ] 13. Verify no duplicated rewards or invalid progression states");
        SevenSinsMod.LOGGER.info("===========================================================");
    }
}
