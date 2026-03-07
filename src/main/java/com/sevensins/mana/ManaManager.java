package com.sevensins.mana;

import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;

/**
 * Utility class for reading and modifying a player's mana.
 *
 * <p>All state is stored exclusively in the player's capability
 * ({@link com.sevensins.character.PlayerCharacterData}); this class holds
 * no state of its own.</p>
 */
public final class ManaManager {

    private ManaManager() {}

    /**
     * Returns the player's current mana.
     *
     * @param player the target player
     * @return current mana, or {@code 0} if the capability is unavailable
     */
    public static int getMana(Player player) {
        return ModCapabilities.get(player)
                .map(cap -> cap.getData().getMana())
                .orElse(0);
    }

    /**
     * Returns the player's maximum mana.
     *
     * @param player the target player
     * @return maximum mana, or {@code 0} if the capability is unavailable
     */
    public static int getMaxMana(Player player) {
        return ModCapabilities.get(player)
                .map(cap -> cap.getData().getMaxMana())
                .orElse(0);
    }

    /**
     * Deducts {@code amount} mana from the player.
     *
     * <p>Mana is clamped to {@code 0} — it will never go negative.</p>
     *
     * @param player the target player
     * @param amount the amount of mana to consume (must be &ge; 0)
     */
    public static void consumeMana(Player player, int amount) {
        ModCapabilities.get(player).ifPresent(cap -> {
            int current = cap.getData().getMana();
            cap.getData().setMana(Math.max(0, current - amount));
        });
    }

    /**
     * Adds {@code amount} mana to the player.
     *
     * <p>Mana is clamped to {@link #getMaxMana(Player)} — it will never
     * exceed the maximum.</p>
     *
     * @param player the target player
     * @param amount the amount of mana to restore (must be &ge; 0)
     */
    public static void restoreMana(Player player, int amount) {
        ModCapabilities.get(player).ifPresent(cap -> {
            int current = cap.getData().getMana();
            int max = cap.getData().getMaxMana();
            cap.getData().setMana(Math.min(max, current + amount));
        });
    }

    /**
     * Returns {@code true} if the player has at least {@code amount} mana.
     *
     * @param player the target player
     * @param amount the required mana amount
     * @return {@code true} when current mana &ge; {@code amount}
     */
    public static boolean hasEnoughMana(Player player, int amount) {
        return getMana(player) >= amount;
    }
}
