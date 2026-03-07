package com.sevensins.character;

import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;

/**
 * Utility class that computes derived player statistics such as Power Level.
 *
 * <p>All calculations are read-only: no capability state is mutated here.</p>
 */
public final class CharacterStats {

    private CharacterStats() {}

    /**
     * Computes the player's current Power Level using the formula:
     *
     * <pre>
     *   powerLevel = (sinLevel × 10) + maxMana + (unlockedAbilities × 25)
     * </pre>
     *
     * <p>If either capability is unavailable the missing component is treated as 0,
     * so this method never throws and never returns a negative result.</p>
     *
     * @param player the player whose power level should be calculated
     * @return computed power level (≥ 0)
     */
    public static int getPowerLevel(Player player) {
        int sinLevel = 0;
        int maxMana = 0;
        int unlockedCount = 0;

        var sinOpt = player.getCapability(ModCapabilities.SIN_DATA).resolve();
        if (sinOpt.isPresent()) {
            sinLevel = sinOpt.get().getSinLevel();
        }

        var charOpt = ModCapabilities.get(player).resolve();
        if (charOpt.isPresent()) {
            PlayerCharacterData data = charOpt.get().getData();
            maxMana = data.getMaxMana();
            unlockedCount = data.getUnlockedAbilities().size();
        }

        return (sinLevel * 10) + maxMana + (unlockedCount * 25);
    }
}
