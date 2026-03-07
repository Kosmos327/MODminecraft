package com.sevensins.dialogue;

import java.util.List;

/**
 * A linear sequence of {@link DialogueNode}s identified by a unique tree ID.
 *
 * <p>Version 1 supports only linear (non-branching) dialogue.  Nodes are
 * traversed in list order from index 0 to the end.</p>
 */
public class DialogueTree {

    private final String id;
    private final List<DialogueNode> nodes;

    public DialogueTree(String id, List<DialogueNode> nodes) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id must not be blank");
        if (nodes == null || nodes.isEmpty()) throw new IllegalArgumentException("nodes must not be empty");
        this.id = id;
        this.nodes = List.copyOf(nodes);
    }

    /** Unique identifier for this dialogue tree. */
    public String getId() {
        return id;
    }

    /** Returns an unmodifiable ordered list of dialogue nodes. */
    public List<DialogueNode> getNodes() {
        return nodes;
    }

    /** Number of nodes in this tree. */
    public int size() {
        return nodes.size();
    }

    /**
     * Returns the node at {@code index}, or {@code null} if out of range.
     */
    public DialogueNode getNode(int index) {
        if (index < 0 || index >= nodes.size()) return null;
        return nodes.get(index);
    }
}
