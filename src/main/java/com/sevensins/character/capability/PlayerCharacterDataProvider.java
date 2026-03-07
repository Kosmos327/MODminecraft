package com.sevensins.character.capability;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.quest.PlayerQuestData;
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
import java.util.Map;
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
            data.copyQuestDataFrom(other);
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

        // Quest and story data
        PlayerQuestData questData = data.getQuestData();
        CompoundTag questTag = new CompoundTag();
        questTag.putString("activeQuestId", questData.getActiveQuestId());

        ListTag completedList = new ListTag();
        for (String id : questData.getCompletedQuestIds()) {
            completedList.add(StringTag.valueOf(id));
        }
        questTag.put("completedQuestIds", completedList);

        CompoundTag progressTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : questData.getQuestProgress().entrySet()) {
            progressTag.putInt(entry.getKey(), entry.getValue());
        }
        questTag.put("questProgress", progressTag);

        ListTag flagsList = new ListTag();
        for (String flag : questData.getStoryFlags()) {
            flagsList.add(StringTag.valueOf(flag));
        }
        questTag.put("storyFlags", flagsList);

        tag.put("questData", questTag);

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

        // Quest and story data
        if (tag.contains("questData", Tag.TAG_COMPOUND)) {
            CompoundTag questTag = tag.getCompound("questData");
            PlayerQuestData questData = data.getQuestData();

            if (questTag.contains("activeQuestId")) {
                questData.setActiveQuestId(questTag.getString("activeQuestId"));
            }
            if (questTag.contains("completedQuestIds", Tag.TAG_LIST)) {
                ListTag completedList = questTag.getList("completedQuestIds", Tag.TAG_STRING);
                for (int i = 0; i < completedList.size(); i++) {
                    questData.addCompletedQuestId(completedList.getString(i));
                }
            }
            if (questTag.contains("questProgress", Tag.TAG_COMPOUND)) {
                CompoundTag progressTag = questTag.getCompound("questProgress");
                for (String key : progressTag.getAllKeys()) {
                    questData.setProgress(key, progressTag.getInt(key));
                }
            }
            if (questTag.contains("storyFlags", Tag.TAG_LIST)) {
                ListTag flagsList = questTag.getList("storyFlags", Tag.TAG_STRING);
                for (int i = 0; i < flagsList.size(); i++) {
                    questData.addStoryFlag(flagsList.getString(i));
                }
            }
        }
    }

    public void invalidate() {
        optional.invalidate();
    }
}
