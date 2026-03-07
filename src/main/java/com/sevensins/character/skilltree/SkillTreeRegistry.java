package com.sevensins.character.skilltree;

import com.sevensins.ability.AbilityType;
import com.sevensins.character.CharacterType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Provides the {@link SkillTreeDefinition} for each {@link CharacterType}.
 *
 * <p>Trees are built once at class-load time.  To extend a tree in the future,
 * add entries here or call {@link #getTree(CharacterType)} and add nodes at
 * startup before players can interact with the skill tree.</p>
 */
public final class SkillTreeRegistry {

    private static final Map<CharacterType, SkillTreeDefinition> REGISTRY =
            new EnumMap<>(CharacterType.class);

    static {
        // WRATH – Meliodas
        REGISTRY.put(CharacterType.MELIODAS, new SkillTreeDefinition()
                .addRoot(AbilityType.HELL_BLAZE)
                .addNode(AbilityType.FULL_COUNTER, AbilityType.HELL_BLAZE)
                .addNode(AbilityType.DEMON_MARK,   AbilityType.FULL_COUNTER)
                .addNode(AbilityType.DEMON_MODE,   AbilityType.DEMON_MARK)
        );

        // PRIDE – Escanor
        REGISTRY.put(CharacterType.ESCANOR, new SkillTreeDefinition()
                .addRoot(AbilityType.CRUEL_SUN)
                .addNode(AbilityType.SUPERNOVA, AbilityType.CRUEL_SUN)
                .addNode(AbilityType.THE_ONE,   AbilityType.SUPERNOVA)
        );

        // GREED – Ban
        REGISTRY.put(CharacterType.BAN, new SkillTreeDefinition()
                .addRoot(AbilityType.SNATCH)
                .addNode(AbilityType.FOX_HUNT,    AbilityType.SNATCH)
                .addNode(AbilityType.HUNTER_FEST, AbilityType.FOX_HUNT)
        );

        // SLOTH – King
        REGISTRY.put(CharacterType.KING, new SkillTreeDefinition()
                .addRoot(AbilityType.SPIRIT_SPEAR)
                .addNode(AbilityType.GUARDIAN, AbilityType.SPIRIT_SPEAR)
                .addNode(AbilityType.INCREASE, AbilityType.GUARDIAN)
        );

        // LUST – Gowther
        REGISTRY.put(CharacterType.GOWTHER, new SkillTreeDefinition()
                .addRoot(AbilityType.MIND_CONTROL)
                .addNode(AbilityType.ILLUSION_BURST,  AbilityType.MIND_CONTROL)
                .addNode(AbilityType.MEMORY_REWRITE,  AbilityType.ILLUSION_BURST)
        );

        // ENVY – Diane
        REGISTRY.put(CharacterType.DIANE, new SkillTreeDefinition()
                .addRoot(AbilityType.TELEPORT)
                .addNode(AbilityType.ARCANE_BURST,   AbilityType.TELEPORT)
                .addNode(AbilityType.INFINITY_MAGIC, AbilityType.ARCANE_BURST)
        );

        // GLUTTONY – Merlin
        REGISTRY.put(CharacterType.MERLIN, new SkillTreeDefinition()
                .addRoot(AbilityType.ENERGY_DRAIN)
                .addNode(AbilityType.DEVOUR,       AbilityType.ENERGY_DRAIN)
                .addNode(AbilityType.ABYSS_SHIELD, AbilityType.DEVOUR)
        );
    }

    private SkillTreeRegistry() {}

    /**
     * Returns the skill tree for the given character, or {@code null} if the
     * character has no registered tree (e.g. {@link CharacterType#NONE}).
     */
    public static SkillTreeDefinition getTree(CharacterType character) {
        return REGISTRY.get(character);
    }
}
