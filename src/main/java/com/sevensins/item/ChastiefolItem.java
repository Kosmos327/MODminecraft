package com.sevensins.item;

import com.sevensins.character.CharacterType;
import net.minecraft.world.item.Item;

/**
 * Spirit Spear Chastiefol — Sacred Treasure of King (Sloth).
 *
 * <p>Version 1 bonuses when wielded by a compatible (KING) player:
 * <ul>
 *   <li>+10% max mana</li>
 *   <li>+15 flat ability damage applied to Sloth abilities</li>
 * </ul>
 *
 * <p>Registered in {@link com.sevensins.registry.ModItems#CHASTIEFOL}.</p>
 */
public class ChastiefolItem extends SacredTreasureItem {

    public ChastiefolItem(Item.Properties properties) {
        super(CharacterType.KING,
                /* abilityDamageBonus */ 15,
                /* meleeDamageBonusPercent */ 0,
                /* manaBonusPercent */ 10,
                properties);
    }
}
