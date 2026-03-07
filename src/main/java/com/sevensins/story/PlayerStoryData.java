package com.sevensins.story;

import com.sevensins.character.CharacterType;

/**
 * Default in-memory implementation of {@link IPlayerStoryData}.
 *
 * <p>In a full implementation this class would be registered as a Forge
 * capability and serialised to the player's NBT data so that state survives
 * log-out/log-in cycles. Those wiring details are marked with TODO.</p>
 */
public class PlayerStoryData implements IPlayerStoryData {

    private CharacterType characterType = CharacterType.NONE;
    private boolean joinedToMeliodasTeam = false;
    private String activeQuest = "";

    // TODO: register this class as a Forge ICapabilityProvider so that
    //       it is automatically attached to every ServerPlayer.

    // TODO: serialise/deserialise fields using CompoundTag (NBT) so that
    //       story progress persists across sessions.

    @Override
    public CharacterType getCharacterType() {
        return characterType;
    }

    @Override
    public void setCharacterType(CharacterType type) {
        this.characterType = (type != null) ? type : CharacterType.NONE;
    }

    @Override
    public boolean isJoinedToMeliodasTeam() {
        return joinedToMeliodasTeam;
    }

    @Override
    public void setJoinedToMeliodasTeam(boolean joined) {
        this.joinedToMeliodasTeam = joined;
    }

    @Override
    public String getActiveQuest() {
        return activeQuest;
    }

    @Override
    public void setActiveQuest(String questKey) {
        this.activeQuest = (questKey != null) ? questKey : "";
    }
}
