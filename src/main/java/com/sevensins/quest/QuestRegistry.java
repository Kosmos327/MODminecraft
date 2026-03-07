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
