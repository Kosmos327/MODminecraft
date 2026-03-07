package com.sevensins.item;

import com.sevensins.character.CharacterType;
import net.minecraft.world.item.Item;

/**
 * Lostvayne — Sacred Treasure of Meliodas (Wrath).
 *
 * <p>Version 1 bonuses when wielded by a compatible (MELIODAS) player:
 * <ul>
 *   <li>+15% melee damage</li>
 *   <li>+10 flat ability damage applied to Wrath abilities</li>
 * </ul>
 *
 * <p>Registered in {@link com.sevensins.registry.ModItems#LOSTVAYNE}.</p>
 */
public class LostvayneItem extends SacredTreasureItem {

    public LostvayneItem(Item.Properties properties) {
        super(CharacterType.MELIODAS,
                /* abilityDamageBonus */ 10,
                /* meleeDamageBonusPercent */ 15,
                /* manaBonusPercent */ 0,
                properties);
    }
}
