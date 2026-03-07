package com.sevensins.character;

import com.sevensins.ability.AbstractAbility;
import com.sevensins.ability.AbilityType;
import com.sevensins.ability.IAbility;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The seven playable characters.  Each character owns exactly three abilities
 * with pre-set mana costs and cooldown durations.
 */
public enum CharacterType {

    MELIODAS(
            ability(AbilityType.FULL_COUNTER,    30, 200),
            ability(AbilityType.HELL_BLAZE,      50, 300),
            ability(AbilityType.REVENGE_COUNTER, 80, 400)
    ),

    BAN(
            ability(AbilityType.SNATCH,          20, 100),
            ability(AbilityType.BANISHING_KILL,  60, 350),
            ability(AbilityType.HUNTER_FEST,     40, 250)
    ),

    KING(
            ability(AbilityType.CHASTIEFOL,      35, 200),
            ability(AbilityType.FORM_BASQUIAS,   50, 300),
            ability(AbilityType.POLLEN_GARDEN,   70, 400)
    ),

    DIANE(
            ability(AbilityType.HEAVY_METAL,     25, 150),
            ability(AbilityType.DOUBLE_HAMMER,   45, 280),
            ability(AbilityType.DROLES_DANCE,    65, 380)
    ),

    MERLIN(
            ability(AbilityType.INFINITY,         40, 300),
            ability(AbilityType.PERFECT_CUBE,     55, 350),
            ability(AbilityType.ABSOLUTE_CANCEL,  70, 450)
    ),

    GOWTHER(
            ability(AbilityType.REWRITE_LIGHT,    35, 250),
            ability(AbilityType.NIGHTMARE_TELLER, 50, 300),
            ability(AbilityType.DOLLS_PLAY,       60, 400)
    ),

    ESCANOR(
            ability(AbilityType.SUNSHINE,         20, 600),
            ability(AbilityType.DIVINE_SWORD,     70, 350),
            ability(AbilityType.THE_ONE,         100, 1200)
    );

    // -------------------------------------------------------------------------

    private final List<IAbility> abilities;

    CharacterType(IAbility... abilities) {
        this.abilities = Collections.unmodifiableList(Arrays.asList(abilities));
    }

    /** Returns the immutable, ordered list of this character's three abilities. */
    public List<IAbility> getAbilities() {
        return abilities;
    }

    // -------------------------------------------------------------------------
    // Helper factory – creates a no-op stub; concrete effects are added later.

    private static IAbility ability(AbilityType type, int manaCost, int cooldownTicks) {
        return new AbstractAbility(type, manaCost, cooldownTicks) {
            @Override
            public void activate(ServerPlayer player) {
                // TODO: implement character-specific ability effect
            }
        };
    }
}
