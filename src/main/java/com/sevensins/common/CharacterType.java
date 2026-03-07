package com.sevensins.common;

/**
 * Represents the playable characters in the Seven Deadly Sins mod.
 * Used by the story system to determine which narrative branch a player follows.
 */
public enum CharacterType {

    /** No character selected (default/unassigned state). */
    NONE,

    /** Meliodas — main story driver of Act 1. */
    MELIODAS,

    /** Diane — starts in a separate zone, pursues a local branch until the reunion event. */
    DIANE;

    /**
     * Returns {@code true} if this is a concrete, selectable character
     * (i.e. not {@link #NONE}).
     */
    public boolean isSelectable() {
        return this != NONE;
    }
}
