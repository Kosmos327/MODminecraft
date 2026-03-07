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

    private final PlayerCharacterDataImpl data = new PlayerCharacterDataImpl();
    private LazyOptional<IPlayerCharacterData> optional = LazyOptional.of(() -> data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return ModCapabilities.PLAYER_CHARACTER_DATA.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        PlayerCharacterData d = data.getData();
        tag.putString("selectedCharacter", d.getSelectedCharacter().getSerializedName());
        tag.putInt("level", d.getLevel());
        tag.putInt("experience", d.getExperience());
        tag.putInt("maxMana", d.getMaxMana());
        tag.putInt("mana", d.getMana());
        tag.putInt("skillPoints", d.getSkillPoints());
        tag.putBoolean("joinedToMeliodasTeam", d.isJoinedToMeliodasTeam());
        tag.putInt("personalStoryStage", d.getPersonalStoryStage());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        PlayerCharacterData d = data.getData();
        if (tag.contains("selectedCharacter")) {
            d.setSelectedCharacter(com.sevensins.character.CharacterType.fromName(tag.getString("selectedCharacter")));
        }
        if (tag.contains("level")) d.setLevel(tag.getInt("level"));
        if (tag.contains("experience")) d.setExperience(tag.getInt("experience"));
        if (tag.contains("maxMana")) d.setMaxMana(tag.getInt("maxMana"));
        if (tag.contains("mana")) d.setMana(tag.getInt("mana"));
        if (tag.contains("skillPoints")) d.setSkillPoints(tag.getInt("skillPoints"));
        if (tag.contains("joinedToMeliodasTeam")) d.setJoinedToMeliodasTeam(tag.getBoolean("joinedToMeliodasTeam"));
        if (tag.contains("personalStoryStage")) d.setPersonalStoryStage(tag.getInt("personalStoryStage"));
    }

    public void invalidate() {
        optional.invalidate();
        optional = LazyOptional.empty();
    }
}
