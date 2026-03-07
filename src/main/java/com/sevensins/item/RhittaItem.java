package com.sevensins.item;

import com.sevensins.character.CharacterType;
import net.minecraft.world.item.Item;

/**
 * Divine Axe Rhitta — Sacred Treasure of Escanor (Pride).
 *
 * <p>Version 1 bonuses when wielded by a compatible (ESCANOR) player:
 * <ul>
 *   <li>+20% melee damage</li>
 *   <li>+15 flat ability damage applied to Pride abilities</li>
 * </ul>
 *
 * <p>Registered in {@link com.sevensins.registry.ModItems#RHITTA}.</p>
 */
public class RhittaItem extends SacredTreasureItem {

    public RhittaItem(Item.Properties properties) {
        super(CharacterType.ESCANOR,
                /* abilityDamageBonus */ 15,
                /* meleeDamageBonusPercent */ 20,
                /* manaBonusPercent */ 0,
                properties);
    }
}
