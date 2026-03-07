package com.sevensins.character.skilltree;

import com.sevensins.ability.AbilityType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all {@link SkillTreeNode}s that belong to one character's skill tree.
 *
 * <p>Nodes are stored in insertion order so that the GUI can iterate them
 * top-to-bottom without extra sorting.</p>
 */
public final class SkillTreeDefinition {

    private final Map<AbilityType, SkillTreeNode> nodes = new LinkedHashMap<>();

    /**
     * Adds a root node (no prerequisite) with cost 1.
     *
     * @param ability ability this node unlocks
     */
    public SkillTreeDefinition addRoot(AbilityType ability) {
        return addNode(ability, null, 1);
    }

    /**
     * Adds a non-root node with cost 1.
     *
     * @param ability      ability this node unlocks
     * @param prerequisite ability that must already be unlocked
     */
    public SkillTreeDefinition addNode(AbilityType ability, AbilityType prerequisite) {
        return addNode(ability, prerequisite, 1);
    }

    /**
     * Adds a node with a custom cost.
     *
     * @param ability      ability this node unlocks
     * @param prerequisite ability that must already be unlocked, or {@code null} for roots
     * @param cost         skill-point cost
     */
    public SkillTreeDefinition addNode(AbilityType ability, AbilityType prerequisite, int cost) {
        nodes.put(ability, new SkillTreeNode(ability, prerequisite, cost));
        return this;
    }

    /** Returns all nodes in this tree in insertion order. */
    public List<SkillTreeNode> getNodes() {
        return List.copyOf(nodes.values());
    }

    /** Returns the node for the given ability, or {@code null} if not part of this tree. */
    public SkillTreeNode getNode(AbilityType ability) {
        return nodes.get(ability);
    }

    /** Returns {@code true} if the ability exists in this tree. */
    public boolean contains(AbilityType ability) {
        return nodes.containsKey(ability);
    }
}
