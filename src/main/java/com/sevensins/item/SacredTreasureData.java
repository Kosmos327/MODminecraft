package com.sevensins.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for reading and writing Sacred Treasure upgrade data on an
 * {@link ItemStack}.
 *
 * <p>The upgrade level is persisted as NBT data on the stack itself, so it
 * travels with the item across saves, servers and inventories.</p>
 */
public final class SacredTreasureData {

    /** Maximum upgrade level for any sacred treasure. */
    public static final int MAX_UPGRADE_LEVEL = 5;

    /** NBT key used to store the upgrade level. */
    static final String TAG_UPGRADE_LEVEL = "seven_sins_upgrade_level";

    private SacredTreasureData() {}

    /**
     * Returns the upgrade level stored on the given stack.
     *
     * <p>Returns {@code 0} if the stack is empty, has no NBT tag, or the tag
     * does not contain an upgrade-level entry. The returned value is always
     * clamped to {@code [0, MAX_UPGRADE_LEVEL]}.</p>
     *
     * @param stack the item stack to query
     * @return upgrade level in [0, {@value #MAX_UPGRADE_LEVEL}]
     */
    public static int getUpgradeLevel(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return 0;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_UPGRADE_LEVEL)) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_UPGRADE_LEVEL, tag.getInt(TAG_UPGRADE_LEVEL)));
    }

    /**
     * Writes the upgrade level onto the given stack's NBT tag.
     *
     * <p>The value is clamped to {@code [0, MAX_UPGRADE_LEVEL]}. Does nothing
     * if the stack is empty.</p>
     *
     * @param stack the item stack to update
     * @param level the upgrade level to store
     */
    public static void setUpgradeLevel(ItemStack stack, int level) {
        if (stack.isEmpty()) {
            return;
        }
        int clamped = Math.max(0, Math.min(MAX_UPGRADE_LEVEL, level));
        stack.getOrCreateTag().putInt(TAG_UPGRADE_LEVEL, clamped);
    }
}
