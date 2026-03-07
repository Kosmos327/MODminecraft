package com.sevensins.item;

import com.sevensins.registry.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Centralises upgrade requirements and upgrade logic for Sacred Treasures.
 *
 * <p>Version 1 requirements per upgrade step:
 * <ul>
 *   <li>+0 → +1 : 2 Sin Fragments, 1 Magic Scroll</li>
 *   <li>+1 → +2 : 3 Sin Fragments, 1 Magic Scroll</li>
 *   <li>+2 → +3 : 4 Sin Fragments, 2 Magic Scrolls</li>
 *   <li>+3 → +4 : 5 Sin Fragments, 2 Magic Scrolls</li>
 *   <li>+4 → +5 : 6 Sin Fragments, 3 Magic Scrolls</li>
 * </ul>
 * Formulas: {@code fragments = currentLevel + 2},
 *            {@code scrolls  = (currentLevel / 2) + 1}.</p>
 *
 * <p>Each upgrade level adds {@value #ABILITY_BONUS_PER_LEVEL} flat
 * ability-damage bonus and {@value #POWER_BONUS_PER_LEVEL} power level.</p>
 */
public final class SacredTreasureUpgradeHelper {

    /** Flat ability-damage bonus added per upgrade level. */
    public static final int ABILITY_BONUS_PER_LEVEL = 5;

    /** Power-level contribution added per upgrade level. */
    public static final int POWER_BONUS_PER_LEVEL = 5;

    private SacredTreasureUpgradeHelper() {}

    // -------------------------------------------------------------------------
    // Requirements
    // -------------------------------------------------------------------------

    /**
     * Returns the number of Sin Fragments required to upgrade from
     * {@code currentLevel} to {@code currentLevel + 1}.
     *
     * @param currentLevel current upgrade level (0–4)
     * @return required fragment count
     */
    public static int requiredFragments(int currentLevel) {
        return currentLevel + 2;
    }

    /**
     * Returns the number of Magic Scrolls required to upgrade from
     * {@code currentLevel} to {@code currentLevel + 1}.
     *
     * @param currentLevel current upgrade level (0–4)
     * @return required scroll count
     */
    public static int requiredScrolls(int currentLevel) {
        return (currentLevel / 2) + 1;
    }

    // -------------------------------------------------------------------------
    // Bonus scaling
    // -------------------------------------------------------------------------

    /**
     * Returns the extra flat ability-damage bonus contributed by the upgrade
     * level stored on the given stack.
     *
     * @param stack the sacred treasure stack
     * @return extra ability-damage bonus (≥ 0)
     */
    public static int getUpgradeAbilityBonus(ItemStack stack) {
        return SacredTreasureData.getUpgradeLevel(stack) * ABILITY_BONUS_PER_LEVEL;
    }

    /**
     * Returns the extra power-level contribution from the upgrade level stored
     * on the given stack.
     *
     * @param stack the sacred treasure stack
     * @return extra power level (≥ 0)
     */
    public static int getUpgradePowerBonus(ItemStack stack) {
        return SacredTreasureData.getUpgradeLevel(stack) * POWER_BONUS_PER_LEVEL;
    }

    // -------------------------------------------------------------------------
    // Upgrade result
    // -------------------------------------------------------------------------

    /** Result of an upgrade attempt. */
    public enum UpgradeResult {
        SUCCESS,
        INVALID_ITEM,
        ALREADY_MAX_LEVEL,
        NOT_ENOUGH_FRAGMENTS,
        NOT_ENOUGH_SCROLLS
    }

    // -------------------------------------------------------------------------
    // Upgrade execution
    // -------------------------------------------------------------------------

    /**
     * Attempts to upgrade the Sacred Treasure held in the player's main hand.
     *
     * <p>On success the materials are consumed, the upgrade level is incremented
     * on the stack, and {@link UpgradeResult#SUCCESS} is returned. If any
     * validation check fails, the inventory is not modified and an appropriate
     * failure result is returned.</p>
     *
     * <p>This method is intended to be called on the <strong>server side</strong>
     * only. Calling it from the client side will have no lasting effect.</p>
     *
     * @param player the player performing the upgrade
     * @return result indicating success or reason for failure
     */
    public static UpgradeResult tryUpgrade(Player player) {
        ItemStack held = player.getMainHandItem();

        // 1 – held item must be a sacred treasure
        if (held.isEmpty() || !(held.getItem() instanceof SacredTreasureItem)) {
            return UpgradeResult.INVALID_ITEM;
        }

        // 2 – must be below max level
        int currentLevel = SacredTreasureData.getUpgradeLevel(held);
        if (currentLevel >= SacredTreasureData.MAX_UPGRADE_LEVEL) {
            return UpgradeResult.ALREADY_MAX_LEVEL;
        }

        // 3 – check material availability
        int neededFragments = requiredFragments(currentLevel);
        int neededScrolls   = requiredScrolls(currentLevel);

        Inventory inv = player.getInventory();
        int hasFragments = countItem(inv, ModItems.SIN_FRAGMENT.get());
        int hasScrolls   = countItem(inv, ModItems.MAGIC_SCROLL.get());

        if (hasFragments < neededFragments) {
            return UpgradeResult.NOT_ENOUGH_FRAGMENTS;
        }
        if (hasScrolls < neededScrolls) {
            return UpgradeResult.NOT_ENOUGH_SCROLLS;
        }

        // 4 – consume materials and apply upgrade
        consumeItem(inv, ModItems.SIN_FRAGMENT.get(), neededFragments);
        consumeItem(inv, ModItems.MAGIC_SCROLL.get(), neededScrolls);
        SacredTreasureData.setUpgradeLevel(held, currentLevel + 1);

        return UpgradeResult.SUCCESS;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static int countItem(Inventory inv, Item item) {
        int count = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == item) {
                count += s.getCount();
            }
        }
        return count;
    }

    private static void consumeItem(Inventory inv, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.getItem() == item) {
                int take = Math.min(remaining, s.getCount());
                s.shrink(take);
                remaining -= take;
            }
        }
    }
}
