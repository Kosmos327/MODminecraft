package com.sevensins.story;

/**
 * Named story flags stored in
 * {@link com.sevensins.quest.PlayerQuestData#getStoryFlags()}.
 *
 * <p>Each flag represents a specific milestone that has been reached.
 * Flags are persisted alongside the player's quest data via NBT.</p>
 */
public enum StoryFlag {

    /** Set after the player completes the "Trial of Awakening" quest. */
    AWAKENING_TRIAL_COMPLETE("awakening_trial_complete");

    private final String id;

    StoryFlag(String id) {
        this.id = id;
    }

    /** The string key stored in the player's story-flag set. */
    public String getId() {
        return id;
    }
}
