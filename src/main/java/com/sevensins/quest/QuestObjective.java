package com.sevensins.quest;

/**
 * Describes a single measurable objective within a {@link Quest}.
 *
 * <p>In Version 1 each quest has one implicit objective whose target count
 * is stored on the {@link Quest} itself.  This class is a thin helper that
 * encapsulates the completion check so it is easy to extend later.</p>
 */
public class QuestObjective {

    private final int targetCount;

    public QuestObjective(int targetCount) {
        if (targetCount <= 0) {
            throw new IllegalArgumentException("targetCount must be positive");
        }
        this.targetCount = targetCount;
    }

    /** The number of qualifying actions required to satisfy this objective. */
    public int getTargetCount() {
        return targetCount;
    }

    /** Returns {@code true} if {@code currentProgress} has reached the target. */
    public boolean isComplete(int currentProgress) {
        return currentProgress >= targetCount;
    }
}
