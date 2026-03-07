package com.sevensins.character;

public class PlayerCharacterData {

    private CharacterType selectedCharacter = CharacterType.NONE;
    private int level = 1;
    private int experience = 0;
    private int mana = 100;
    private int maxMana = 100;
    private int skillPoints = 0;
    private boolean joinedToMeliodasTeam = false;
    private int personalStoryStage = 0;

    public CharacterType getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(CharacterType selectedCharacter) {
        this.selectedCharacter = selectedCharacter;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public int getMana() {
        return mana;
    }

    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = skillPoints;
    }

    public boolean isJoinedToMeliodasTeam() {
        return joinedToMeliodasTeam;
    }

    public void setJoinedToMeliodasTeam(boolean joinedToMeliodasTeam) {
        this.joinedToMeliodasTeam = joinedToMeliodasTeam;
    }

    public int getPersonalStoryStage() {
        return personalStoryStage;
    }

    public void setPersonalStoryStage(int personalStoryStage) {
        this.personalStoryStage = personalStoryStage;
    }

    public void addExperience(int amount) {
        this.experience += amount;
        levelUpIfNeeded();
    }

    public int getXpToNextLevel() {
        return level * 100;
    }

    public void levelUpIfNeeded() {
        int xpNeeded;
        while (experience >= (xpNeeded = getXpToNextLevel())) {
            experience -= xpNeeded;
            level++;
            skillPoints++;
        }
    }

    public void restoreMana(int amount) {
        mana = Math.min(mana + amount, maxMana);
    }

    public void consumeMana(int amount) {
        mana = Math.max(mana - amount, 0);
    }
}
