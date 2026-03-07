package com.sevensins.common.capability;

import com.sevensins.character.CharacterType;

/**
 * Capability interface that holds sin-related player data.
 */
public interface ISinData {

    /** Returns the character currently assigned to the player, or {@code null} if none. */
    CharacterType getCharacter();

    /** Assigns a character to the player. */
    void setCharacter(CharacterType character);
}
