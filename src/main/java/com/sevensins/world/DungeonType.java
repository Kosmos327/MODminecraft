package com.sevensins.world;

/**
 * Identifies every dungeon type supported by the mod.
 *
 * <p>Version 1 ships only {@link #DEMON_CAVE}.  Additional types can be added
 * here without changing any other API surfaces.</p>
 */
public enum DungeonType {

    /** A demonic underground cave encounter.  Version-1 dungeon. */
    DEMON_CAVE("Demon Cave");

    private final String displayName;

    DungeonType(String displayName) {
        this.displayName = displayName;
    }

    /** Human-readable name used in chat messages and logs. */
    public String getDisplayName() {
        return displayName;
    }
}
