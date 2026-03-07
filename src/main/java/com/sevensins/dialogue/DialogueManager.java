package com.sevensins.dialogue;

import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.network.ModNetwork;
import com.sevensins.network.packet.TriggerDialoguePacket;
import com.sevensins.quest.PlayerQuestData;
import com.sevensins.quest.QuestManager;
import com.sevensins.quest.QuestRegistry;
import com.sevensins.story.StoryFlag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Central server-side manager for the dialogue system.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Maintains a registry of all {@link DialogueTree}s.</li>
 *   <li>Tracks one active {@link DialogueSession} per player (UUID).</li>
 *   <li>Sends all dialogue lines to the client as a single
 *       {@link TriggerDialoguePacket}.</li>
 *   <li>Executes server-side action callbacks when a player finishes
 *       a dialogue (via {@link com.sevensins.network.packet.AdvanceDialoguePacket}).</li>
 * </ul>
 *
 * <p>Singleton — obtain via {@link #getInstance()}.</p>
 */
public class DialogueManager {

    private static final DialogueManager INSTANCE = new DialogueManager();

    /** Returns the singleton {@link DialogueManager}. */
    public static DialogueManager getInstance() {
        return INSTANCE;
    }

    // -----------------------------------------------------------------------
    // Internal state
    // -----------------------------------------------------------------------

    private final Map<String, DialogueTree> trees = new HashMap<>();
    /**
     * One session per player UUID; present while the player is viewing a
     * dialogue, removed after the action has been executed.
     */
    private final Map<UUID, DialogueSession> activeSessions = new ConcurrentHashMap<>();

    // -----------------------------------------------------------------------
    // Constructor — registers built-in nodes and Meliodas trees
    // -----------------------------------------------------------------------

    private DialogueManager() {
        registerDefaultNodes();
        NPCDialogue.buildMeliodasTrees().forEach(this::registerTree);
    }

    // -----------------------------------------------------------------------
    // Tree and node registry
    // -----------------------------------------------------------------------

    /** Registers a {@link DialogueTree} by its ID. */
    public void registerTree(DialogueTree tree) {
        trees.put(tree.getId(), tree);
    }

    /** Returns the tree with the given ID, if any. */
    public Optional<DialogueTree> getTree(String id) {
        return Optional.ofNullable(trees.get(id));
    }

    // -----------------------------------------------------------------------
    // NPC interaction entry-point
    // -----------------------------------------------------------------------

    /**
     * Called by an NPC entity when a player right-clicks it.
     *
     * <p>Determines which {@link DialogueTree} to show (based on the player's
     * story flags), sends all its lines to the client in a single
     * {@link TriggerDialoguePacket}, and records a {@link DialogueSession}
     * so the appropriate action is executed when the player finishes.</p>
     *
     * @param player  the interacting server player
     * @param npcId   identifier used to choose the correct tree (currently the
     *                NPC's entity-type registry name, e.g. {@code "meliodas_npc"})
     */
    public void onPlayerInteract(ServerPlayer player, String npcId) {
        if (player == null || npcId == null) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerQuestData questData = cap.getData().getQuestData();

            String treeId;
            String action = null;

            if ("meliodas_npc".equals(npcId)) {
                if (questData.hasStoryFlag(StoryFlag.FIRST_DEMONS_COMPLETE.getId())) {
                    treeId = NPCDialogue.MELIODAS_CHAPTER2_COMPLETE;
                } else if (questData.hasStoryFlag(StoryFlag.FIRST_DEMONS_STARTED.getId())) {
                    treeId = NPCDialogue.MELIODAS_CHAPTER2_REMINDER;
                } else if (questData.hasStoryFlag(StoryFlag.AWAKENING_TRIAL_COMPLETE.getId())) {
                    treeId = NPCDialogue.MELIODAS_CHAPTER2_INTRO;
                    action = NPCDialogue.ACTION_START_CHAPTER2;
                } else {
                    treeId = NPCDialogue.MELIODAS_NEUTRAL;
                }
            } else {
                // Unknown NPC — fallback to plain message
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal("..."));
                return;
            }

            final String resolvedAction = action;
            getTree(treeId).ifPresentOrElse(
                    tree -> startDialogue(player, tree, resolvedAction),
                    () -> player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal("..."))
            );
        });
    }

    // -----------------------------------------------------------------------
    // Dialogue start
    // -----------------------------------------------------------------------

    /**
     * Starts a dialogue session for {@code player}: sends all lines of
     * {@code tree} to the client and registers the session.
     *
     * <p>If the player already has an active session it is replaced.</p>
     *
     * @param player  the server player
     * @param tree    the tree to display
     * @param action  optional action key to execute when the player finishes
     *                (may be {@code null})
     */
    public void startDialogue(ServerPlayer player, DialogueTree tree, String action) {
        activeSessions.put(player.getUUID(), new DialogueSession(tree.getId(), action));
        sendTree(player, tree);
    }

    // -----------------------------------------------------------------------
    // Packet dispatch
    // -----------------------------------------------------------------------

    private void sendTree(ServerPlayer player, DialogueTree tree) {
        List<String[]> lines = new ArrayList<>();
        for (DialogueNode node : tree.getNodes()) {
            lines.add(new String[]{ node.getSpeakerName(), node.getText() });
        }
        ModNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player),
                new TriggerDialoguePacket(lines));
    }

    // -----------------------------------------------------------------------
    // Dialogue finish (called from AdvanceDialoguePacket handler)
    // -----------------------------------------------------------------------

    /**
     * Called when the player has acknowledged all dialogue lines.
     * Executes any pending action and clears the session.
     *
     * @param player the server player who finished the dialogue
     */
    public void onDialogueFinished(ServerPlayer player) {
        if (player == null) return;
        DialogueSession session = activeSessions.remove(player.getUUID());
        if (session == null) return;

        if (session.getPendingAction() != null) {
            executeAction(player, session.getPendingAction());
        }
    }

    // -----------------------------------------------------------------------
    // Action dispatch
    // -----------------------------------------------------------------------

    private void executeAction(ServerPlayer player, String action) {
        if (NPCDialogue.ACTION_START_CHAPTER2.equals(action)) {
            ModCapabilities.get(player).ifPresent(cap -> {
                PlayerQuestData questData = cap.getData().getQuestData();
                questData.addStoryFlag(StoryFlag.TALKED_TO_MELIODAS.getId());
                questData.addStoryFlag(StoryFlag.FIRST_DEMONS_STARTED.getId());
            });
            QuestManager.assignQuest(player, QuestRegistry.FIRST_DEMON_HUNT_ID);
        }
    }

    // -----------------------------------------------------------------------
    // Legacy: flat node registry kept for backwards compatibility
    // -----------------------------------------------------------------------

    private final Map<String, DialogueNode> nodes = new HashMap<>();

    private void registerDefaultNodes() {
        register(new DialogueNode(
                "elizabeth_intro",
                "Elizabeth",
                "Oh! Are you perhaps a Holy Knight? Please, I need your help to find the Seven Deadly Sins!",
                List.of()));
        register(new DialogueNode(
                "meliodas_intro",
                "Meliodas",
                "Welcome to the Boar Hat! Want something to drink? Or maybe you're looking for the Seven Deadly Sins?",
                List.of()));
        register(new DialogueNode(
                "diane_intro",
                "Diane",
                "Hm? A tiny human? Don't get in my way unless you want to get stepped on!",
                List.of()));
    }

    private void register(DialogueNode node) {
        nodes.put(node.getId(), node);
    }

    /** Returns the flat node with the given ID (legacy), if any. */
    public Optional<DialogueNode> getNode(String id) {
        return Optional.ofNullable(nodes.get(id));
    }
}
