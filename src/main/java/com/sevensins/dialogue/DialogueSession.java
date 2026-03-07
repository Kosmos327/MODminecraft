package com.sevensins.dialogue;

/**
 * Tracks the server-side state of an in-progress dialogue for a single player.
 *
 * <p>Only one dialogue session per player is allowed at a time.
 * The session stores the ID of the action to execute once the player
 * acknowledges the last node.</p>
 */
public class DialogueSession {

    private final String treeId;
    /**
     * Optional identifier for the server-side action to run when the player
     * finishes the dialogue (e.g. {@code "meliodas_chapter2_start"}).
     * May be {@code null} if no action is needed.
     */
    private final String pendingAction;

    public DialogueSession(String treeId, String pendingAction) {
        if (treeId == null || treeId.isEmpty()) {
            throw new IllegalArgumentException("treeId must not be blank");
        }
        this.treeId = treeId;
        this.pendingAction = pendingAction;
    }

    /** The ID of the {@link DialogueTree} this session is presenting. */
    public String getTreeId() {
        return treeId;
    }

    /**
     * The action key to execute on the server when the dialogue ends,
     * or {@code null} if there is no associated action.
     */
    public String getPendingAction() {
        return pendingAction;
    }
}
