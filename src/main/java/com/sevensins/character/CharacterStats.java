package com.sevensins.character;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.item.SacredTreasureData;
import com.sevensins.item.SacredTreasureItem;
import com.sevensins.item.SacredTreasureUpgradeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class that computes derived player statistics such as Power Level.
 *
 * <p>All calculations are read-only: no capability state is mutated here.</p>
 */
public final class CharacterStats {

    /** Flat power-level bonus granted when a compatible sacred treasure is held. */
    private static final int SACRED_TREASURE_POWER_BONUS = 50;

    private CharacterStats() {}

    /**
     * Computes the player's current Power Level using the formula:
     *
     * <pre>
     *   powerLevel = (sinLevel × 10) + maxMana + (unlockedAbilities × 25)
     *              + sacredTreasureBonus (50 if compatible treasure held)
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

        int base = (sinLevel * 10) + maxMana + (unlockedCount * 25);

        // Add sacred treasure power bonus if the player holds a compatible treasure
        ItemStack heldStack = player.getMainHandItem();
        SacredTreasureItem treasure = heldStack.getItem() instanceof SacredTreasureItem t ? t : null;
        boolean compatible = treasure != null && treasure.isCompatible(player);

        int sacredBonus   = compatible ? SACRED_TREASURE_POWER_BONUS : 0;
        int upgradeBonus  = compatible
                ? SacredTreasureUpgradeHelper.getUpgradePowerBonus(heldStack) : 0;

        return base + sacredBonus + upgradeBonus;
    }

    // -------------------------------------------------------------------------
    // Sacred Treasure helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the {@link SacredTreasureItem} held in the player's main hand,
     * or {@code null} if no sacred treasure is currently held.
     *
     * @param player the player to inspect
     * @return the sacred treasure in the main hand, or {@code null}
     */
    public static SacredTreasureItem getEquippedSacredTreasure(Player player) {
        ItemStack held = player.getMainHandItem();
        if (held.getItem() instanceof SacredTreasureItem treasure) {
            return treasure;
        }
        return null;
    }

    /**
     * Returns the flat ability-damage bonus from the sacred treasure held in
     * the player's main hand, if that treasure is compatible with the ability.
     *
     * <p>Version 1 rule: a treasure bonus applies to an ability when both the
     * treasure and the ability belong to the same character type.</p>
     *
     * @param player      the wielding player
     * @param abilityType the ability being used
     * @return flat damage bonus (≥ 0)
     */
    public static int getAbilityDamageBonus(Player player, AbilityType abilityType) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof SacredTreasureItem treasure)) {
            return 0;
        }
        if (!treasure.isCompatible(player)) {
            return 0;
        }
        CharacterType abilityOwner = getCharacterForAbility(abilityType);
        if (abilityOwner != CharacterType.NONE && abilityOwner == treasure.getLinkedCharacter()) {
            return treasure.rawAbilityDamageBonus()
                    + SacredTreasureUpgradeHelper.getUpgradeAbilityBonus(held);
        }
        return 0;
    }

    /**
     * Returns the max-mana increase (in units) granted by the sacred treasure
     * held in the player's main hand, if compatible.
     *
     * <p>The mana bonus is expressed as a percentage of the player's base max
     * mana from the capability (e.g. 10 = +10%).</p>
     *
     * @param player the wielding player
     * @return additional max-mana units (≥ 0)
     */
    public static int getMaxManaBonus(Player player) {
        SacredTreasureItem treasure = getEquippedSacredTreasure(player);
        if (treasure == null) {
            return 0;
        }
        int pct = treasure.getManaBonus(player);
        if (pct <= 0) {
            return 0;
        }
        int baseMana = ModCapabilities.get(player).resolve()
                .map(cap -> cap.getData().getMaxMana())
                .orElse(0);
        return (baseMana * pct) / 100;
    }

    // -------------------------------------------------------------------------
    // Ability → CharacterType mapping
    // -------------------------------------------------------------------------

    /**
     * Maps an {@link AbilityType} to the {@link CharacterType} it belongs to.
     * Returns {@link CharacterType#NONE} for legacy or unmapped abilities.
     *
     * @param ability the ability to look up
     * @return the owning character type, or {@link CharacterType#NONE}
     */
    public static CharacterType getCharacterForAbility(AbilityType ability) {
        return switch (ability) {
            case HELL_BLAZE, FULL_COUNTER, DEMON_MARK, DEMON_MODE -> CharacterType.MELIODAS;
            case CRUEL_SUN, SUPERNOVA, THE_ONE                    -> CharacterType.ESCANOR;
            case SNATCH, FOX_HUNT, HUNTER_FEST                    -> CharacterType.BAN;
            case SPIRIT_SPEAR, GUARDIAN, INCREASE                 -> CharacterType.KING;
            case MIND_CONTROL, ILLUSION_BURST, MEMORY_REWRITE     -> CharacterType.GOWTHER;
            case TELEPORT, ARCANE_BURST, INFINITY_MAGIC, EARTH_SMASH -> CharacterType.DIANE;
            case ENERGY_DRAIN, DEVOUR, ABYSS_SHIELD               -> CharacterType.MERLIN;
            default                                               -> CharacterType.NONE;
        };
    }
}

