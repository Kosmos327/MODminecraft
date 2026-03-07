package com.sevensins.character.capability;

import com.sevensins.character.PlayerCharacterData;

public class PlayerCharacterDataImpl implements IPlayerCharacterData {

    private final PlayerCharacterData data = new PlayerCharacterData();

    @Override
    public PlayerCharacterData getData() {
        return data;
    }

    @Override
    public void copyFrom(PlayerCharacterData other) {
        data.setSelectedCharacter(other.getSelectedCharacter());
        data.setLevel(other.getLevel());
        data.setExperience(other.getExperience());
        data.setMaxMana(other.getMaxMana());
        data.setMana(other.getMana());
        data.setSkillPoints(other.getSkillPoints());
        data.setJoinedToMeliodasTeam(other.isJoinedToMeliodasTeam());
        data.setPersonalStoryStage(other.getPersonalStoryStage());
    }
}
