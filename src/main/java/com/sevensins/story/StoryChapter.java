package com.sevensins.story;

/**
 * Story chapter identifiers for the Seven Deadly Sins RPG.
 *
 * <p>Each chapter maps to a numeric {@code stage} value that is stored in
 * {@link com.sevensins.character.PlayerCharacterData#getPersonalStoryStage()}
 * so that the chapter persists via the existing NBT capability.</p>
 */
public enum StoryChapter {

    /** No chapter active (character not yet selected). */
    NONE(0),

    /** Chapter 1 — "The Awakening of Sin". */
    AWAKENING(1);

    private final int stage;

    StoryChapter(int stage) {
        this.stage = stage;
    }

    /** The integer stage value stored in player data. */
    public int getStage() {
        return stage;
    }

    /**
     * Returns the {@link StoryChapter} whose stage equals {@code stage},
     * or {@link #NONE} if no match is found.
     */
    public static StoryChapter fromStage(int stage) {
        for (StoryChapter c : values()) {
            if (c.stage == stage) return c;
        }
        return NONE;
    }
}
