package com.sevensins.dialogue;

import java.util.List;

/**
 * Factory for all pre-defined NPC dialogue trees.
 *
 * <p>Each constant is the tree ID that should be passed to
 * {@link DialogueManager#startDialogue(net.minecraft.server.level.ServerPlayer, String, String)}.</p>
 */
public final class NPCDialogue {

    // -----------------------------------------------------------------------
    // Tree IDs
    // -----------------------------------------------------------------------

    /** Meliodas's Chapter 2 introduction — played once after the Awakening trial. */
    public static final String MELIODAS_CHAPTER2_INTRO = "meliodas_chapter2_intro";

    /** Meliodas's Chapter 2 reminder — played while the demon hunt is in progress. */
    public static final String MELIODAS_CHAPTER2_REMINDER = "meliodas_chapter2_reminder";

    /** Meliodas's Chapter 2 completion dialogue — played after the hunt is finished. */
    public static final String MELIODAS_CHAPTER2_COMPLETE = "meliodas_chapter2_complete";

    /** Meliodas's neutral dialogue — played before the Awakening trial is complete. */
    public static final String MELIODAS_NEUTRAL = "meliodas_neutral";

    // -----------------------------------------------------------------------
    // Action IDs (used by DialogueManager to trigger server-side effects)
    // -----------------------------------------------------------------------

    /** Action executed at the end of the Chapter 2 intro: sets flags + assigns quest. */
    public static final String ACTION_START_CHAPTER2 = "action_start_chapter2";

    // -----------------------------------------------------------------------
    // Tree construction
    // -----------------------------------------------------------------------

    private NPCDialogue() {}

    /**
     * Builds and returns all Meliodas dialogue trees.
     * Called once during {@link DialogueManager} initialisation.
     */
    public static List<DialogueTree> buildMeliodasTrees() {
        return List.of(
                buildChapter2Intro(),
                buildChapter2Reminder(),
                buildChapter2Complete(),
                buildNeutral()
        );
    }

    private static DialogueTree buildChapter2Intro() {
        return new DialogueTree(
                MELIODAS_CHAPTER2_INTRO,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "So... your Sin has awakened. But this is only the beginning.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "Dark creatures have started appearing nearby. You must test your power.",
                                List.of()),
                        new DialogueNode(
                                "node3", "Meliodas",
                                "Defeat 3 powerful hostile creatures and return stronger.",
                                List.of(), ACTION_START_CHAPTER2)
                )
        );
    }

    private static DialogueTree buildChapter2Reminder() {
        return new DialogueTree(
                MELIODAS_CHAPTER2_REMINDER,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "The demons are still out there. Defeat 3 powerful creatures \u2014 don't let your guard down.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildChapter2Complete() {
        return new DialogueTree(
                MELIODAS_CHAPTER2_COMPLETE,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "You've grown stronger. The first demons have been dealt with \u2014 but darker threats await.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildNeutral() {
        return new DialogueTree(
                MELIODAS_NEUTRAL,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "Welcome to the Boar Hat. Prove yourself first \u2014 then we'll talk.",
                                List.of())
                )
        );
    }
}
