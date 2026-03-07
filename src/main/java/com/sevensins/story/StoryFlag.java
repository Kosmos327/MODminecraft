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

    /** Set when the player first encounters Estarossa. */
    ESTAROSSA_ENCOUNTERED("estarossa_encountered"),

    /** Set when Estarossa first transitions to Phase 2 in the player's encounter. */
    ESTAROSSA_PHASE_2_SEEN("estarossa_phase_2_seen"),

    /** Set when Estarossa enters the ENRAGED phase in the player's encounter. */
    ESTAROSSA_ENRAGED_SEEN("estarossa_enraged_seen"),

    /** Set after the player defeats Estarossa. */
    ESTAROSSA_SLAIN("estarossa_slain");

    private final String id;

    StoryFlag(String id) {
        this.id = id;
    }

    /** The string key stored in the player's story-flag set. */
    public String getId() {
        return id;
    }
}
