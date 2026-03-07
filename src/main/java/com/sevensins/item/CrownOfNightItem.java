package com.sevensins.item;

/**
 * Crown of Night — the first Legendary Artifact of the Seven Deadly Sins RPG.
 *
 * <p>An endgame dark relic forged from the essence of defeated demons.
 * It channels demonic energy into raw power, granting the wearer enhanced
 * mana reserves, resilience, and battle focus.</p>
 *
 * <h2>Version-1 bonuses (active when held in main hand or offhand)</h2>
 * <ul>
 *   <li>+{@value #MANA_BONUS_PERCENT}% Max Mana</li>
 *   <li>+{@value #RESISTANCE_BONUS_PERCENT}% Resistance</li>
 *   <li>+{@value #POWER_LEVEL_BONUS} Power Level</li>
 * </ul>
 *
 * <p>Registered in {@link com.sevensins.registry.ModItems#CROWN_OF_NIGHT}.</p>
 */
public class CrownOfNightItem extends LegendaryArtifactItem {

    /** Max-mana increase percentage. */
    public static final int MANA_BONUS_PERCENT = 20;

    /** Resistance bonus percentage. */
    public static final int RESISTANCE_BONUS_PERCENT = 10;

    /** Flat power-level bonus. */
    public static final int POWER_LEVEL_BONUS = 5;

    public CrownOfNightItem(Properties properties) {
        super("Crown of Night",
                MANA_BONUS_PERCENT,
                RESISTANCE_BONUS_PERCENT,
                POWER_LEVEL_BONUS,
                properties);
    }
}
