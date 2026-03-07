package com.sevensins.ability;

import com.sevensins.ability.impl.DianeEarthSmashAbility;
import com.sevensins.ability.impl.DemonModeAbility;
import com.sevensins.ability.impl.MeliodasFullCounterAbility;
import com.sevensins.ability.impl.TheOneAbility;
import com.sevensins.ability.impl.wrath.HellBlazeAbility;
import com.sevensins.character.CharacterType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry that maps each {@link CharacterType} to the list of
 * {@link Ability} instances available to that character.
 *
 * <p>New characters and abilities can be registered at any time via
 * {@link #register(CharacterType, Ability)}.  The registry is pre-populated
 * with the initial set of abilities on class load.
 */
public class AbilityManager {

    private static final Ability FULL_COUNTER = new MeliodasFullCounterAbility();
    private static final Ability HELL_BLAZE = new HellBlazeAbility();
    private static final Ability EARTH_SMASH = new DianeEarthSmashAbility();
    private static final Ability DEMON_MODE = new DemonModeAbility();
    private static final Ability THE_ONE = new TheOneAbility();

    private static final Map<CharacterType, List<Ability>> REGISTRY =
            new EnumMap<>(CharacterType.class);

    static {
        REGISTRY.put(CharacterType.MELIODAS, List.of(HELL_BLAZE, FULL_COUNTER, DEMON_MODE));
        REGISTRY.put(CharacterType.DIANE, List.of(EARTH_SMASH));
        REGISTRY.put(CharacterType.ESCANOR, List.of(THE_ONE));
    }

    private AbilityManager() {}

    /**
     * Returns the list of abilities for the given character type.
     * Returns an empty list for characters that have no abilities registered yet.
     */
    public static List<Ability> getAbilitiesFor(CharacterType type) {
        return REGISTRY.getOrDefault(type, Collections.emptyList());
    }

    /**
     * Adds {@code ability} to the list of abilities for the given
     * {@code characterType}.  Creates a new list if none exists yet.
     */
    public static void register(CharacterType characterType, Ability ability) {
        REGISTRY.merge(
                characterType,
                List.of(ability),
                (existing, added) -> {
                    java.util.List<Ability> merged = new java.util.ArrayList<>(existing);
                    merged.addAll(added);
                    return Collections.unmodifiableList(merged);
                }
        );
    }
}
