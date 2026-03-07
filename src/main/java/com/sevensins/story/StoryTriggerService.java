package com.sevensins.story;

import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.quest.PlayerQuestData;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.world.DungeonType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Drives the story flow for Chapter 1: "The Awakening of Sin".
 *
 * <p>Acts as a bridge between the character system, quest system, and story
 * progression.  All public methods are safe to call from the server thread;
 * missing capability data is handled without throwing.</p>
 *
 * <p>Singleton — obtain via {@link #getInstance()}.</p>
 */
public final class StoryTriggerService {

    private static final StoryTriggerService INSTANCE = new StoryTriggerService();

    private StoryTriggerService() {}

    /** Returns the singleton {@link StoryTriggerService}. */
    public static StoryTriggerService getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Character selection hook
    // -------------------------------------------------------------------------

    /**
     * Called after a player successfully selects a {@link CharacterType}.
     *
     * <ul>
     *   <li>Begins Chapter 1 ({@link StoryChapter#AWAKENING}) if no chapter
     *       is active yet.</li>
     *   <li>Assigns the {@value QuestRegistry#AWAKENING_TRIAL_ID} quest if
     *       the player has not already completed or started it.</li>
     * </ul>
     *
     * @param player        the server-side player who selected a character
     * @param characterType the chosen {@link CharacterType}
     */
    public void onCharacterSelected(ServerPlayer player, CharacterType characterType) {
        if (player == null || characterType == null || !characterType.isSelectable()) {
            return;
        }

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData charData = cap.getData();

            // Begin Chapter 1 if not already started
            if (charData.getPersonalStoryStage() == StoryChapter.NONE.getStage()) {
                charData.setPersonalStoryStage(StoryChapter.AWAKENING.getStage());
                player.sendSystemMessage(Component.literal("Your Sin has awakened..."));
            }

            // Assign the first quest if not already active or completed
            checkAndAssignFirstQuest(player, charData);
        });
    }

    // -------------------------------------------------------------------------
    // Quest completion hook
    // -------------------------------------------------------------------------

    /**
     * Called by {@link QuestManager} after a quest is completed.
     *
     * <p>Handles story advancement for known quest IDs.</p>
     *
     * @param player  the server-side player who completed the quest
     * @param questId the ID of the completed quest
     */
    public void onQuestCompleted(ServerPlayer player, String questId) {
        if (player == null || questId == null) return;

        if (QuestRegistry.AWAKENING_TRIAL_ID.equals(questId)) {
            onAwakeningTrialComplete(player);
        } else if (QuestRegistry.FIRST_DEMON_HUNT_ID.equals(questId)) {
            onFirstDemonHuntComplete(player);
        } else if (QuestRegistry.SLAY_RED_DEMON_ID.equals(questId)) {
            onRedDemonSlain(player);
        } else if (QuestRegistry.CLEAR_DEMON_CAVE_ID.equals(questId)) {
            onDemonCaveQuestComplete(player);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Assigns the first quest ({@value QuestRegistry#AWAKENING_TRIAL_ID}) if
     * the player has a valid character, has not completed it, and has no
     * active quest.
     *
     * <p>Called on character selection and on login, so players who relog
     * before completing the quest can still receive it.</p>
     */
    public void checkAndAssignFirstQuest(ServerPlayer player, PlayerCharacterData charData) {
        if (charData.getSelectedCharacter() == CharacterType.NONE) return;
        PlayerQuestData questData = charData.getQuestData();
        if (!questData.isCompleted(QuestRegistry.AWAKENING_TRIAL_ID)
                && questData.getActiveQuestId().isEmpty()) {
            QuestManager.assignQuest(player, QuestRegistry.AWAKENING_TRIAL_ID);
        }
    }

    private void onAwakeningTrialComplete(ServerPlayer player) {
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData questData = cap.getData().getQuestData();
            questData.addStoryFlag(StoryFlag.AWAKENING_TRIAL_COMPLETE.getId());
        });
    }

    private void onFirstDemonHuntComplete(ServerPlayer player) {
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData questData = cap.getData().getQuestData();
            questData.addStoryFlag(StoryFlag.FIRST_DEMONS_COMPLETE.getId());
            cap.getData().setPersonalStoryStage(StoryChapter.FIRST_DEMONS.getStage());
            player.sendSystemMessage(
                    Component.literal("Chapter complete: you have faced the first demons."));
            // Begin Chapter 3 — the Red Demon threatens the land
            QuestManager.assignQuest(player, QuestRegistry.SLAY_RED_DEMON_ID);
        });
    }

    private void onRedDemonSlain(ServerPlayer player) {
        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData questData = cap.getData().getQuestData();
            questData.addStoryFlag(StoryFlag.RED_DEMON_SLAIN.getId());
            cap.getData().setPersonalStoryStage(StoryChapter.RED_DEMON.getStage());
            player.sendSystemMessage(Component.literal("You have slain the Red Demon!"));
            // Begin Chapter 4 — demonic corruption remains underground
            player.sendSystemMessage(Component.literal(
                    "Demonic corruption lingers underground. Seek out the Demon Cave."));
            QuestManager.assignQuest(player, QuestRegistry.CLEAR_DEMON_CAVE_ID);
        });
    }

    private void onDemonCaveQuestComplete(ServerPlayer player) {
        ModCapabilities.get(player).ifPresent(cap -> {
            // DEMON_CAVE_CLEARED flag is already set by onDungeonCleared, which is
            // called unconditionally from DungeonManager. Here we only advance the
            // story chapter and show the completion message.
            cap.getData().setPersonalStoryStage(StoryChapter.DEMON_CAVE.getStage());
            player.sendSystemMessage(
                    Component.literal("Chapter complete: the Demon Cave has been purged."));
        });
    }

    /**
     * Called by {@link com.sevensins.world.DungeonManager} when the player first
     * enters a dungeon.  Sets the entry story flag for the dungeon type.
     *
     * @param player the entering player
     * @param type   the {@link DungeonType} entered
     */
    public void onDungeonEntered(ServerPlayer player, DungeonType type) {
        if (player == null || type == null) return;

        if (type == DungeonType.DEMON_CAVE) {
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData()
                            .addStoryFlag(StoryFlag.DEMON_CAVE_STARTED.getId()));
        }
    }

    /**
     * Called by {@link com.sevensins.world.DungeonManager} when a dungeon is
     * cleared.  Sets the clear story flag for the dungeon type.
     *
     * @param player the player who cleared the dungeon
     * @param type   the {@link DungeonType} that was cleared
     */
    public void onDungeonCleared(ServerPlayer player, DungeonType type) {
        if (player == null || type == null) return;

        if (type == DungeonType.DEMON_CAVE) {
            ModCapabilities.get(player).ifPresent(cap ->
                    cap.getData().getQuestData()
                            .addStoryFlag(StoryFlag.DEMON_CAVE_CLEARED.getId()));
        }
    }
}
