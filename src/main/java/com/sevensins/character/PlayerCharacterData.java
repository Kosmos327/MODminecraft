package com.sevensins.character;

public class PlayerCharacterData {

    private String selectedCharacter = "";
    private int level = 1;
    private int experience = 0;
    private float mana = 100.0f;
    private float maxMana = 100.0f;
    private int skillPoints = 0;
    private boolean joinedToMeliodasTeam = false;
    private int personalStoryStage = 0;

    public String getSelectedCharacter() {
        return selectedCharacter;
    }

    public void setSelectedCharacter(String selectedCharacter) {
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

    public float getMana() {
        return mana;
    }

    public void setMana(float mana) {
        this.mana = mana;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(float maxMana) {
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
}
