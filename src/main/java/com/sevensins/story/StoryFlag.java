package com.sevensins.story;

/**
 * Named story flags stored in
 * {@link com.sevensins.quest.PlayerQuestData#getStoryFlags()}.
 *
 * <p>Each flag represents a specific milestone that has been reached.
 * Flags are persisted alongside the player's quest data via NBT.</p>
 */
public enum StoryFlag {

    /** Set after the player completes the "Trial of Awakening" quest. */
    AWAKENING_TRIAL_COMPLETE("awakening_trial_complete"),

    /** Set when the player first talks to the mentor NPC (Meliodas). */
    TALKED_TO_MELIODAS("talked_to_meliodas"),

    /** Set when Chapter 2 ("The First Demons") has been started. */
    FIRST_DEMONS_STARTED("first_demons_started"),

    /** Set when the player completes the Chapter 2 demon-hunt quest. */
    FIRST_DEMONS_COMPLETE("first_demons_complete"),

    /** Set when the player defeats the Red Demon boss. */
    RED_DEMON_SLAIN("red_demon_slain"),

    /** Set when the player first enters a Demon Cave dungeon. */
    DEMON_CAVE_STARTED("demon_cave_started"),

    /** Set when the player successfully clears a Demon Cave dungeon. */
    DEMON_CAVE_CLEARED("demon_cave_cleared"),

    /** Set when the player obtains Lostvayne for the first time. */
    OBTAINED_LOSTVAYNE("obtained_lostvayne"),

    /** Set when the player obtains the Divine Axe Rhitta for the first time. */
    OBTAINED_RHITTA("obtained_rhitta"),

    /** Set when the player obtains Spirit Spear Chastiefol for the first time. */
    OBTAINED_CHASTIEFOL("obtained_chastiefol"),

    /** Set when the player defeats the Gray Demon boss. */
    GRAY_DEMON_SLAIN("gray_demon_slain"),

    /** Set when the player first encounters (spawns) the Gray Demon. */
    GRAY_DEMON_ENCOUNTERED("gray_demon_encountered"),

    /** Set the first time the Gray Demon enters its second phase. */
    GRAY_DEMON_PHASE2_SEEN("gray_demon_phase2_seen"),

    /** Set when the player defeats the Demon Commander boss. */
    DEMON_COMMANDER_SLAIN("demon_commander_slain"),

    /** Set when the player first encounters (spawns) the Demon Commander. */
    DEMON_COMMANDER_ENCOUNTERED("demon_commander_encountered"),

    /** Set the first time the Demon Commander enters its second phase. */
    DEMON_COMMANDER_PHASE2_SEEN("demon_commander_phase2_seen"),

    /** Set the first time the Demon Commander summons minions. */
    DEMON_COMMANDER_SUMMONS_SEEN("demon_commander_summons_seen");

    private final String id;

    StoryFlag(String id) {
        this.id = id;
    }

    /** The string key stored in the player's story-flag set. */
    public String getId() {
        return id;
    }
}
