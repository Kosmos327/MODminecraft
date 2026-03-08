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

    /** Meliodas's hint after the Red Demon is slain — points toward the Demon Cave. */
    public static final String MELIODAS_RED_DEMON_HINT = "meliodas_red_demon_hint";

    /** Meliodas's hint after the Demon Cave is cleared — Gray Demon is rising. */
    public static final String MELIODAS_CAVE_CLEARED_HINT = "meliodas_cave_cleared_hint";

    /** Meliodas's hint after the Gray Demon is slain — Demon Commander mobilises. */
    public static final String MELIODAS_GRAY_DEMON_HINT = "meliodas_gray_demon_hint";

    /** Meliodas's words after the Demon Commander falls — Estarossa approaches. */
    public static final String MELIODAS_COMMANDER_FALLEN_HINT = "meliodas_commander_fallen_hint";

    /** Meliodas's guidance when the player is on the Estarossa quest. */
    public static final String MELIODAS_ESTAROSSA_HINT = "meliodas_estarossa_hint";

    /** Meliodas's reaction after Estarossa is slain — the Demon King looms. */
    public static final String MELIODAS_ESTAROSSA_SLAIN = "meliodas_estarossa_slain";

    /** Meliodas's final words — the Demon King must be confronted. */
    public static final String MELIODAS_ENDGAME_HINT = "meliodas_endgame_hint";

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
                buildNeutral(),
                buildRedDemonHint(),
                buildCaveClearedHint(),
                buildGrayDemonHint(),
                buildCommanderFallenHint(),
                buildEstarossaHint(),
                buildEstarossaSlain(),
                buildEndgameHint()
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

    private static DialogueTree buildRedDemonHint() {
        return new DialogueTree(
                MELIODAS_RED_DEMON_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "Good work on the Red Demon. But the corruption doesn't stop there.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "There's a cave nearby oozing demonic energy. Clear it out before it spreads.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildCaveClearedHint() {
        return new DialogueTree(
                MELIODAS_CAVE_CLEARED_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "The cave is cleared. You're getting the hang of this.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "But I'm sensing a stronger demonic presence. A Gray Demon \u2014 much more dangerous than a Red one.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildGrayDemonHint() {
        return new DialogueTree(
                MELIODAS_GRAY_DEMON_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "The Gray Demon is down. You're stronger than I thought.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "There's a Demon Commander rallying the remaining forces. Stop them \u2014 before things get out of hand.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildCommanderFallenHint() {
        return new DialogueTree(
                MELIODAS_COMMANDER_FALLEN_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "The Demon Commander has fallen. Not bad.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "But I can feel it \u2014 something far worse is coming. One of the Ten Commandments.",
                                List.of()),
                        new DialogueNode(
                                "node3", "Meliodas",
                                "Estarossa. The Commandment of Love. Be ready.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildEstarossaHint() {
        return new DialogueTree(
                MELIODAS_ESTAROSSA_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "Estarossa is no ordinary enemy. His Commandment nullifies those who can feel love.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "Fight with your sin. Don't hold back.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildEstarossaSlain() {
        return new DialogueTree(
                MELIODAS_ESTAROSSA_SLAIN,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "You defeated Estarossa. I won't lie \u2014 I didn't think you could.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "But this isn't over. The Demon King himself has taken notice of you.",
                                List.of()),
                        new DialogueNode(
                                "node3", "Meliodas",
                                "If you want to end this... you'll have to face him.",
                                List.of())
                )
        );
    }

    private static DialogueTree buildEndgameHint() {
        return new DialogueTree(
                MELIODAS_ENDGAME_HINT,
                List.of(
                        new DialogueNode(
                                "node1", "Meliodas",
                                "The Demon King. The ruler of Purgatory. This is the final fight.",
                                List.of()),
                        new DialogueNode(
                                "node2", "Meliodas",
                                "Make sure your Sacred Treasure is with you. You'll need every advantage you can get.",
                                List.of())
                )
        );
    }
}
