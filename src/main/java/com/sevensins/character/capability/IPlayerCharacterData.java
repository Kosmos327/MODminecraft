package com.sevensins.character.capability;

import com.sevensins.character.PlayerCharacterData;

public interface IPlayerCharacterData {

    PlayerCharacterData getData();

    void copyFrom(PlayerCharacterData other);
}
