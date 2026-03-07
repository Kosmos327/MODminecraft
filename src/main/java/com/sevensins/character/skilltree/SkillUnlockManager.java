package com.sevensins.character.skilltree;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;
import com.sevensins.character.PlayerCharacterData;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-side logic for unlocking skill tree nodes.
 *
 * <p>All public methods must be called on the server thread.</p>
 */
public final class SkillUnlockManager {

    private SkillUnlockManager() {}

    /**
     * Attempts to unlock {@code abilityType} for the given player.
     *
     * <p>Unlock rules:
     * <ol>
     *   <li>Player must have a selected {@link CharacterType} (not NONE).</li>
     *   <li>The ability must belong to that character's skill tree.</li>
     *   <li>Player must not already have it unlocked.</li>
     *   <li>If the node has a prerequisite, that prerequisite must already be unlocked.</li>
     *   <li>Player must have enough skill points (≥ node cost).</li>
     * </ol>
     *
     * @param player      the server player trying to unlock an ability
     * @param abilityType the ability to unlock
     * @return {@link UnlockResult} describing the outcome
     */
    public static UnlockResult tryUnlock(ServerPlayer player, AbilityType abilityType) {
        final UnlockResult[] result = {UnlockResult.NO_CHARACTER};

        ModCapabilities.get(player).ifPresent(cap -> {
            PlayerCharacterData data = cap.getData();
            CharacterType character = data.getSelectedCharacter();

            if (character == CharacterType.NONE) {
                result[0] = UnlockResult.NO_CHARACTER;
                return;
            }

            SkillTreeDefinition tree = SkillTreeRegistry.getTree(character);
            if (tree == null || !tree.contains(abilityType)) {
                result[0] = UnlockResult.NOT_IN_TREE;
                return;
            }

            if (data.hasUnlockedAbility(abilityType)) {
                result[0] = UnlockResult.ALREADY_UNLOCKED;
                return;
            }

            SkillTreeNode node = tree.getNode(abilityType);
            AbilityType prereq = node.getPrerequisite();
            if (prereq != null && !data.hasUnlockedAbility(prereq)) {
                result[0] = UnlockResult.MISSING_PREREQUISITE;
                return;
            }

            if (data.getSkillPoints() < node.getCost()) {
                result[0] = UnlockResult.NOT_ENOUGH_POINTS;
                return;
            }

            // All checks passed – spend points and record the unlock.
            data.setSkillPoints(data.getSkillPoints() - node.getCost());
            data.unlockAbility(abilityType);
            result[0] = UnlockResult.SUCCESS;
        });

        return result[0];
    }

    /** Possible outcomes of an unlock attempt. */
    public enum UnlockResult {
        /** The unlock succeeded. */
        SUCCESS,
        /** Player has not selected a character. */
        NO_CHARACTER,
        /** The ability does not belong to the player's tree. */
        NOT_IN_TREE,
        /** The ability is already unlocked. */
        ALREADY_UNLOCKED,
        /** A required prerequisite is not yet unlocked. */
        MISSING_PREREQUISITE,
        /** Player does not have enough skill points. */
        NOT_ENOUGH_POINTS
    }
}
