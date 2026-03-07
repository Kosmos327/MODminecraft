package com.sevensins.character.skilltree;

import com.sevensins.ability.AbilityType;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single node in a character's skill tree.
 *
 * <p>Each node holds an {@link AbilityType}, an optional prerequisite (the ability
 * that must be unlocked first), and the skill-point cost to unlock it.</p>
 */
public final class SkillTreeNode {

    private final AbilityType ability;
    @Nullable
    private final AbilityType prerequisite;
    private final int cost;

    /**
     * @param ability      the ability this node unlocks
     * @param prerequisite the ability that must already be unlocked, or {@code null} for root nodes
     * @param cost         skill points required to unlock
     */
    public SkillTreeNode(AbilityType ability, @Nullable AbilityType prerequisite, int cost) {
        if (ability == null || ability == AbilityType.NONE) {
            throw new IllegalArgumentException("ability must not be null or NONE");
        }
        if (cost < 1) {
            throw new IllegalArgumentException("cost must be at least 1");
        }
        this.ability = ability;
        this.prerequisite = prerequisite;
        this.cost = cost;
    }

    /** The ability unlocked by this node. */
    public AbilityType getAbility() {
        return ability;
    }

    /**
     * The ability that must be unlocked before this node can be purchased,
     * or {@code null} if this is a root node.
     */
    @Nullable
    public AbilityType getPrerequisite() {
        return prerequisite;
    }

    /** Skill-point cost to unlock this node. */
    public int getCost() {
        return cost;
    }
}
