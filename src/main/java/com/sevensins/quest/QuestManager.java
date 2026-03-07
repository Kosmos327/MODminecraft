package com.sevensins.quest;

import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.story.StoryTriggerService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central service for quest assignment, progress tracking, and completion.
 *
 * <p>All methods are safe to call from the server thread.  Capability access
 * is guarded so missing data never causes a crash.</p>
 */
public final class QuestManager {

    /** XP awarded when the player completes a quest. */
    public static final int QUEST_COMPLETION_XP = 100;

    private QuestManager() {}

    // -------------------------------------------------------------------------
    // Quest assignment
    // -------------------------------------------------------------------------

    /**
     * Assigns {@code questId} to {@code player} if:
     * <ul>
     *   <li>the quest exists in {@link QuestRegistry},</li>
     *   <li>the player has not already completed it, and</li>
     *   <li>the player has no other active quest.</li>
     * </ul>
     * Sends a chat message to the player on success.
     */
    public static void assignQuest(ServerPlayer player, String questId) {
        if (player == null || questId == null) return;

        QuestRegistry.getQuest(questId).ifPresent(quest ->
                ModCapabilities.get(player).ifPresent(cap -> {
                    PlayerQuestData questData = cap.getData().getQuestData();
                    if (questData.isCompleted(questId)) return;
                    if (!questData.getActiveQuestId().isEmpty()) return;

                    questData.setActiveQuestId(questId);
                    player.sendSystemMessage(
                            Component.literal("Quest started: " + quest.getTitle()));
                })
        );
    }

    // -------------------------------------------------------------------------
    // Progress
    // -------------------------------------------------------------------------

    /**
     * Increments the kill-quest counter for {@code player} by one.
     * If the active quest is not a {@link QuestType#KILL} quest this is a no-op.
     * Completes the quest automatically when the target is reached.
     */
    public static void incrementKillProgress(ServerPlayer player) {
        if (player == null) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData charData = cap.getData();
            PlayerQuestData questData = charData.getQuestData();
            String activeId = questData.getActiveQuestId();
            if (activeId.isEmpty()) return;

            QuestRegistry.getQuest(activeId).ifPresent(quest -> {
                if (quest.getType() != QuestType.KILL) return;

                int current = questData.getProgress(activeId);
                int newProgress = current + 1;
                questData.setProgress(activeId, newProgress);

                player.sendSystemMessage(Component.literal(
                        "Quest progress: " + newProgress + " / " + quest.getTargetValue()));

                if (newProgress >= quest.getTargetValue()) {
                    completeQuest(player, quest, charData);
                }
            });
        });
    }

    // -------------------------------------------------------------------------
    // Completion
    // -------------------------------------------------------------------------

    /**
     * Marks {@code quest} as complete for {@code player}, grants rewards, and
     * notifies {@link com.sevensins.story.StoryTriggerService}.
     */
    private static void completeQuest(ServerPlayer player, Quest quest,
                                      PlayerCharacterData charData) {
        charData.getQuestData().completeQuest(quest.getId());

        // XP reward
        charData.addExperience(QUEST_COMPLETION_XP);
        charData.levelUpIfNeeded();

        player.sendSystemMessage(
                Component.literal("Quest complete: " + quest.getTitle()));

        // Notify story system
        StoryTriggerService.getInstance().onQuestCompleted(player, quest.getId());
    }
}
