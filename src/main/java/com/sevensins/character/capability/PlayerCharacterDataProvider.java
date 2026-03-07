package com.sevensins.character.capability;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

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
            data.setUnlockedAbilities(other.getUnlockedAbilities());
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
        tag.putString("selectedCharacter", data.getSelectedCharacter().getSerializedName());
        tag.putInt("level", data.getLevel());
        tag.putInt("experience", data.getExperience());
        tag.putInt("mana", data.getMana());
        tag.putInt("maxMana", data.getMaxMana());
        tag.putInt("skillPoints", data.getSkillPoints());
        tag.putBoolean("joinedToMeliodasTeam", data.isJoinedToMeliodasTeam());
        tag.putInt("personalStoryStage", data.getPersonalStoryStage());

        ListTag abilitiesList = new ListTag();
        for (AbilityType ability : data.getUnlockedAbilities()) {
            abilitiesList.add(StringTag.valueOf(ability.getSerializedName()));
        }
        tag.put("unlockedAbilities", abilitiesList);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("selectedCharacter")) {
            data.setSelectedCharacter(
                    CharacterType.fromName(tag.getString("selectedCharacter")));
        }
        if (tag.contains("level")) {
            data.setLevel(tag.getInt("level"));
        }
        if (tag.contains("experience")) {
            data.setExperience(tag.getInt("experience"));
        }
        if (tag.contains("mana")) {
            data.setMana(tag.getInt("mana"));
        }
        if (tag.contains("maxMana")) {
            data.setMaxMana(tag.getInt("maxMana"));
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
        if (tag.contains("unlockedAbilities", Tag.TAG_LIST)) {
            Set<AbilityType> unlocked = EnumSet.noneOf(AbilityType.class);
            ListTag list = tag.getList("unlockedAbilities", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                AbilityType ability = AbilityType.fromName(list.getString(i));
                if (ability != AbilityType.NONE) {
                    unlocked.add(ability);
                }
            }
            data.setUnlockedAbilities(unlocked);
        }
    }

    public void invalidate() {
        optional.invalidate();
    }
}
