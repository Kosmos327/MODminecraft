package com.sevensins.mana;

import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;

/**
 * Service that encapsulates server-side mana-regeneration logic.
 *
 * <p>Centralising regen in one place allows all callers to benefit from the
 * same optimisations without duplicating guard logic.</p>
 *
 * <h2>Optimisations applied</h2>
 * <ul>
 *   <li><strong>Max-mana skip</strong>: players already at their maximum mana
 *       are skipped entirely, avoiding a capability lookup on every regen
 *       interval for fully-charged players.</li>
 *   <li><strong>Null / offline guard</strong>: invalid or disconnected players
 *       are skipped without touching state.</li>
 * </ul>
 *
 * <p>Stateless singleton – all methods are static.</p>
 */
public final class ManaRegenService {

    private ManaRegenService() {}

    /**
     * Applies {@code amount} mana regeneration to {@code player} if the player
     * is valid and not already at maximum mana.
     *
     * <p>This method is safe to call every tick (or on an interval); it will
     * simply no-op when no work is needed.</p>
     *
     * @param player the server-side player to regenerate mana for
     * @param amount the amount of mana to restore (must be &ge; 0)
     */
    public static void applyRegen(ServerPlayer player, int amount) {
        if (player == null || !player.isAlive()) return;

        ModCapabilities.get(player).ifPresent(cap -> {
            int current = cap.getData().getMana();
            int max     = cap.getData().getMaxMana();

            // Skip entirely when the player is already at max – no work to do.
            if (current >= max) return;

            cap.getData().setMana(Math.min(max, current + amount));
        });
    }
}
