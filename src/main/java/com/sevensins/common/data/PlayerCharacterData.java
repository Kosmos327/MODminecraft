package com.sevensins.common.data;

/**
 * Holds per-player character-selection state.
 * Stored in the player's capability data and persisted across sessions.
 */
public class PlayerCharacterData {

    /** The character this player has chosen, or {@code null} if none yet. */
    private CharacterType selectedCharacter = null;

    /**
     * Stage of the personal story arc for this player.
     * {@code 0} = not started; positive values indicate progress.
     * For Meliodas the stage is set to {@code 1} upon initial selection.
     */
    private int personalStoryStage = 0;

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public CharacterType getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(CharacterType selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public boolean hasSelectedCharacter() {
        return selectedCharacter != null;
    }

    public int getPersonalStoryStage() {
        return personalStoryStage;
    }

    public void setPersonalStoryStage(int stage) {
        if (stage < 0) throw new IllegalArgumentException("personalStoryStage must be non-negative, got: " + stage);
        this.personalStoryStage = stage;
    }
}
