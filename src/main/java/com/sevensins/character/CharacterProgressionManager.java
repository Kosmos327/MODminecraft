package com.sevensins.character;

import com.sevensins.character.capability.ISinData;
import com.sevensins.character.capability.ModCapabilities;
import com.sevensins.network.ModNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Utility class that manages sin-level XP gain and level-up logic.
 *
 * <p>XP is stored in the player's {@link ISinData} capability. When accumulated XP
 * reaches the threshold ({@code level × XP_PER_LEVEL}) the sin level increases, max mana
 * is updated, and the player receives a chat notification.</p>
 */
public final class CharacterProgressionManager {

    /** Maximum attainable sin level. */
    public static final int MAX_SIN_LEVEL = 100;

    /** XP required per level (multiplied by the current level). e.g. level 1 needs 100, level 2 needs 200. */
    private static final int XP_PER_LEVEL = 100;

    /** Base max-mana value awarded at sin level 1. */
    private static final int BASE_MAX_MANA = 100;

    /** Additional max mana granted for each sin level gained. */
    private static final int MANA_PER_LEVEL = 10;

    /** Translation key for the level-up chat message. */
    private static final String MSG_LEVEL_UP = "message.seven_sins.progression.level_up";

    private CharacterProgressionManager() {}

    /**
     * Adds {@code amount} XP to the player and triggers a level-up check.
     *
     * <p>XP is only awarded when the player has already chosen a
     * {@link CharacterType} (i.e. not {@link CharacterType#NONE}).</p>
     *
     * @param player the server-side player receiving XP
     * @param amount the amount of XP to add (negative values are ignored)
     */
    public static void addXP(ServerPlayer player, int amount) {
        if (amount <= 0) return;

        ModCapabilities.get(player).ifPresent(charCap -> {
            if (charCap.getData().getSelectedCharacter() == CharacterType.NONE) return;

            player.getCapability(ModCapabilities.SIN_DATA).ifPresent(sinData -> {
                sinData.addSinExperience(amount);
                checkLevelUp(player, sinData);
            });
        });
    }

    /**
     * Checks whether the player's accumulated XP warrants a level-up and
     * processes all pending level-ups in a single pass.
     *
     * <p>For each level gained:
     * <ul>
     *   <li>Sin level is incremented (capped at {@value #MAX_SIN_LEVEL}).</li>
     *   <li>XP is reduced by the threshold that was consumed ({@code level × XP_PER_LEVEL}).</li>
     *   <li>Max mana is updated to {@code BASE_MAX_MANA + (sinLevel × MANA_PER_LEVEL)}.</li>
     *   <li>A chat message is sent to the player.</li>
     * </ul>
     * After all level-ups the updated {@link ISinData} is synced to the client.</p>
     *
     * @param player  the server-side player to evaluate
     * @param sinData the player's sin capability data (already retrieved)
     */
    public static void checkLevelUp(ServerPlayer player, ISinData sinData) {
        boolean leveledUp = false;
        int level = sinData.getSinLevel();

        while (level < MAX_SIN_LEVEL) {
            int required = level * XP_PER_LEVEL;
            if (sinData.getSinExperience() < required) break;

            sinData.setSinExperience(sinData.getSinExperience() - required);
            level++;
            sinData.setSinLevel(level);
            leveledUp = true;

            // Increase max mana: BASE_MAX_MANA + (sinLevel × MANA_PER_LEVEL)
            // Also grant +1 skill point for each sin level gained.
            final int newLevel = level;
            ModCapabilities.get(player).ifPresent(charCap -> {
                int newMaxMana = BASE_MAX_MANA + (newLevel * MANA_PER_LEVEL);
                charCap.getData().setMaxMana(newMaxMana);
                charCap.getData().setSkillPoints(charCap.getData().getSkillPoints() + 1);
            });

            player.sendSystemMessage(Component.translatable(MSG_LEVEL_UP, level));
        }

        if (leveledUp) {
            ModNetwork.syncToPlayer(sinData, player);
        }
    }
}
