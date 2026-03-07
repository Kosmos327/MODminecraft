package com.sevensins.item;

import net.minecraft.world.item.Item;

/**
 * A magical scroll containing arcane knowledge or spell components.
 *
 * <p>Awarded as a chance-based dungeon reward.  Future versions may allow the
 * player to right-click to activate an effect or unlock an ability.</p>
 *
 * <p>Registered in {@link com.sevensins.registry.ModItems#MAGIC_SCROLL}.</p>
 */
public class MagicScrollItem extends Item {

    public MagicScrollItem(Properties properties) {
        super(properties);
    }
}
