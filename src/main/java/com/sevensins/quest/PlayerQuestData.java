package com.sevensins.quest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerQuestData {

    private String activeQuestId = "";
    private final Set<String> completedQuestIds = new HashSet<>();
    private final Map<String, Integer> questProgress = new HashMap<>();
    private final Set<String> storyFlags = new HashSet<>();

    public PlayerQuestData() {}

    // -------------------------------------------------------------------------
    // Active quest
    // -------------------------------------------------------------------------

    public String getActiveQuestId() {
        return activeQuestId;
    }

    public void setActiveQuestId(String questId) {
        this.activeQuestId = questId != null ? questId : "";
    }

    // -------------------------------------------------------------------------
    // Completed quests
    // -------------------------------------------------------------------------

    public Set<String> getCompletedQuestIds() {
        return Collections.unmodifiableSet(completedQuestIds);
    }

    public boolean isCompleted(String questId) {
        return completedQuestIds.contains(questId);
    }

    /** Marks a quest as completed, clears its progress, and clears the active quest if it matches. */
    public void completeQuest(String questId) {
        completedQuestIds.add(questId);
        questProgress.remove(questId);
        if (activeQuestId.equals(questId)) {
            activeQuestId = "";
        }
    }

    /** Adds to completed set without touching activeQuestId or progress (used during NBT restore). */
    public void addCompletedQuestId(String questId) {
        completedQuestIds.add(questId);
    }

    // -------------------------------------------------------------------------
    // Quest progress
    // -------------------------------------------------------------------------

    public Map<String, Integer> getQuestProgress() {
        return Collections.unmodifiableMap(questProgress);
    }

    public int getProgress(String questId) {
        return questProgress.getOrDefault(questId, 0);
    }

    public void setProgress(String questId, int progress) {
        if (progress < 0) {
            throw new IllegalArgumentException("progress must not be negative");
        }
        questProgress.put(questId, progress);
    }

    // -------------------------------------------------------------------------
    // Story flags
    // -------------------------------------------------------------------------

    public void addStoryFlag(String flag) {
        if (flag != null && !flag.isEmpty()) {
            storyFlags.add(flag);
        }
    }

    public boolean hasStoryFlag(String flag) {
        return flag != null && storyFlags.contains(flag);
    }

    public Set<String> getStoryFlags() {
        return Collections.unmodifiableSet(storyFlags);
    }

    // -------------------------------------------------------------------------
    // Copy
    // -------------------------------------------------------------------------

    public void copyFrom(PlayerQuestData other) {
        this.activeQuestId = other.activeQuestId;
        this.completedQuestIds.clear();
        this.completedQuestIds.addAll(other.completedQuestIds);
        this.questProgress.clear();
        this.questProgress.putAll(other.questProgress);
        this.storyFlags.clear();
        this.storyFlags.addAll(other.storyFlags);
    }
}
