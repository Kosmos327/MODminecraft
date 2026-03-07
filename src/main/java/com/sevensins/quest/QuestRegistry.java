package com.sevensins.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Static registry of all predefined quests.
 *
 * <p>Quests are registered at class-load time.  Add new quests by calling
 * {@link #register(Quest)} inside the static initialiser.</p>
 */
public final class QuestRegistry {

    /** ID of the first story quest assigned to every new player. */
    public static final String AWAKENING_TRIAL_ID = "awakening_trial";

    /** ID of the Chapter 2 demon-hunt quest. */
    public static final String FIRST_DEMON_HUNT_ID = "first_demon_hunt";

    /** ID of the Chapter 3 Red Demon boss quest. */
    public static final String SLAY_RED_DEMON_ID = "slay_red_demon";

    /** ID of the Chapter 4 Demon Cave dungeon quest. */
    public static final String CLEAR_DEMON_CAVE_ID = "clear_demon_cave";

    // Sacred Treasure quest placeholder IDs (scaffold for future story chapters)

    /** ID of the Chapter 5 Estarossa boss quest. */
    public static final String SLAY_ESTAROSSA_ID = "slay_estarossa";

    /** ID of the quest to obtain the Divine Axe Rhitta. */
    public static final String OBTAIN_RHITTA_ID = "obtain_rhitta";

    /** ID of the quest to obtain Spirit Spear Chastiefol. */
    public static final String OBTAIN_CHASTIEFOL_ID = "obtain_chastiefol";

    // Endgame quest IDs

    /** ID of the endgame quest to survive a Night Demon Raid. */
    public static final String SURVIVE_NIGHT_RAID_ID = "survive_night_raid";

    /** ID of the endgame quest to slay a Mythic demon. */
    public static final String SLAY_MYTHIC_DEMON_ID = "slay_mythic_demon";

    /** ID of the endgame quest to obtain a Legendary Artifact. */
    public static final String OBTAIN_LEGENDARY_ARTIFACT_ID = "obtain_legendary_artifact";

    private static final Map<String, Quest> QUESTS = new HashMap<>();

    static {
        register(new Quest(
                AWAKENING_TRIAL_ID,
                "Trial of Awakening",
                "Defeat 5 hostile creatures to awaken your Sin.",
                QuestType.KILL,
                5
        ));
        register(new Quest(
                FIRST_DEMON_HUNT_ID,
                "The First Demons",
                "Defeat 3 powerful hostile creatures (max health \u2265 20) threatening the land.",
                QuestType.KILL,
                3
        ));
        register(new Quest(
                SLAY_RED_DEMON_ID,
                "The Red Demon",
                "Defeat the Red Demon threatening the land.",
                QuestType.KILL,
                1
        ));
        register(new Quest(
                CLEAR_DEMON_CAVE_ID,
                "Into the Demon Cave",
                "Enter and clear a Demon Cave corrupted by demonic energy.",
                QuestType.DUNGEON_CLEAR,
                1
        ));
        register(new Quest(
                SURVIVE_NIGHT_RAID_ID,
                "Survive the Night",
                "Survive a Night Demon Raid and defeat all three waves.",
                QuestType.DUNGEON_CLEAR,
                1
        ));
        register(new Quest(
                SLAY_MYTHIC_DEMON_ID,
                "Slay the Mythic Demon",
                "Defeat the Mythic Red Demon that appears in the final raid wave.",
                QuestType.KILL,
                1
        ));
        register(new Quest(
                OBTAIN_LEGENDARY_ARTIFACT_ID,
                "Claim a Legendary Artifact",
                "Obtain your first Legendary Artifact from the endgame reward pool.",
                QuestType.COLLECT,
                1
        ));
    }

    private QuestRegistry() {}

    /** Registers a quest.  Throws if an identical ID is already registered. */
    private static void register(Quest quest) {
        if (QUESTS.containsKey(quest.getId())) {
            throw new IllegalStateException("Duplicate quest id: " + quest.getId());
        }
        QUESTS.put(quest.getId(), quest);
    }

    /**
     * Returns the {@link Quest} for the given ID, or {@link Optional#empty()}
     * if no quest with that ID exists.
     */
    public static Optional<Quest> getQuest(String id) {
        if (id == null || id.isEmpty()) return Optional.empty();
        return Optional.ofNullable(QUESTS.get(id));
    }
}
