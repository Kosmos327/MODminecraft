package com.sevensins.character.capability;

import com.sevensins.character.PlayerCharacterData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCharacterDataProvider implements ICapabilitySerializable<CompoundTag> {

    private final PlayerCharacterData data = new PlayerCharacterData();
    private final IPlayerCharacterData instance = new IPlayerCharacterData() {
        @Override
        public PlayerCharacterData getData() {
            return data;
        }

        @Override
        public void copyFrom(PlayerCharacterData other) {
            data.setSelectedCharacter(other.getSelectedCharacter());
            data.setLevel(other.getLevel());
            data.setExperience(other.getExperience());
            data.setMana(other.getMana());
            data.setMaxMana(other.getMaxMana());
            data.setSkillPoints(other.getSkillPoints());
            data.setJoinedToMeliodasTeam(other.isJoinedToMeliodasTeam());
            data.setPersonalStoryStage(other.getPersonalStoryStage());
        }
    };
    private final LazyOptional<IPlayerCharacterData> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_CHARACTER_DATA.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("selectedCharacter", data.getSelectedCharacter());
        tag.putInt("level", data.getLevel());
        tag.putInt("experience", data.getExperience());
        tag.putFloat("mana", data.getMana());
        tag.putFloat("maxMana", data.getMaxMana());
        tag.putInt("skillPoints", data.getSkillPoints());
        tag.putBoolean("joinedToMeliodasTeam", data.isJoinedToMeliodasTeam());
        tag.putInt("personalStoryStage", data.getPersonalStoryStage());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("selectedCharacter")) {
            data.setSelectedCharacter(tag.getString("selectedCharacter"));
        }
        if (tag.contains("level")) {
            data.setLevel(tag.getInt("level"));
        }
        if (tag.contains("experience")) {
            data.setExperience(tag.getInt("experience"));
        }
        if (tag.contains("mana")) {
            data.setMana(tag.getFloat("mana"));
        }
        if (tag.contains("maxMana")) {
            data.setMaxMana(tag.getFloat("maxMana"));
        }
        if (tag.contains("skillPoints")) {
            data.setSkillPoints(tag.getInt("skillPoints"));
        }
        if (tag.contains("joinedToMeliodasTeam")) {
            data.setJoinedToMeliodasTeam(tag.getBoolean("joinedToMeliodasTeam"));
        }
        if (tag.contains("personalStoryStage")) {
            data.setPersonalStoryStage(tag.getInt("personalStoryStage"));
        }
    }

    public void invalidate() {
        optional.invalidate();
    }
}
