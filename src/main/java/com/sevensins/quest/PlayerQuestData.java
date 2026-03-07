package com.sevensins.quest;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerQuestData {

    private final Set<String> completedQuestIds;
    private final Map<String, Integer> questProgress;

    public PlayerQuestData() {
        this.completedQuestIds = new HashSet<>();
        this.questProgress = new HashMap<>();
    }

    public Set<String> getCompletedQuestIds() {
        return Collections.unmodifiableSet(completedQuestIds);
    }

    public Map<String, Integer> getQuestProgress() {
        return Collections.unmodifiableMap(questProgress);
    }

    public boolean isCompleted(String questId) {
        return completedQuestIds.contains(questId);
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

    public void completeQuest(String questId) {
        completedQuestIds.add(questId);
        questProgress.remove(questId);
    }
}
