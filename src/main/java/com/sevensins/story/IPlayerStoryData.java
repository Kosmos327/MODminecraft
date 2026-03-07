package com.sevensins.story;

import com.sevensins.character.CharacterType;

/**
 * Holds per-player story state for Act 1.
 *
 * <p>Implementations are attached to each {@code ServerPlayer} and persisted
 * across sessions. The default implementation is {@link PlayerStoryData}.</p>
 */
public interface IPlayerStoryData {

    /**
     * Returns the {@link CharacterType} the player has chosen,
     * or {@link CharacterType#NONE} if the player has not yet selected a character.
     */
    CharacterType getCharacterType();

    /** Sets the player's chosen character. */
    void setCharacterType(CharacterType type);

    /**
     * Returns {@code true} if Diane has joined Meliodas's team
     * (i.e. the <em>reunion</em> event has fired for this player).
     */
    boolean isJoinedToMeliodasTeam();

    /** Marks this player as having completed the reunion event. */
    void setJoinedToMeliodasTeam(boolean joined);

    /**
     * Returns the key of the quest currently active for this player,
     * or {@code null} / empty string if no quest is active.
     */
    String getActiveQuest();

    /** Assigns an active quest by its string key (e.g. {@code "meliodas_start"}). */
    void setActiveQuest(String questKey);
}
