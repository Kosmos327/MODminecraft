package com.sevensins.ability;

import com.sevensins.character.CharacterType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Manages active ultimate forms for all players.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Start / stop an ultimate form.</li>
 *   <li>Query whether an ultimate (or a specific one) is currently active.</li>
 *   <li>Expose remaining duration.</li>
 *   <li>Apply / remove temporary MobEffect bonuses.</li>
 *   <li>Expire active ultimates automatically on each server tick.</li>
 *   <li>Clean up state on player death or logout.</li>
 * </ul>
 */
public final class UltimateAbilityManager {

    /** Per-player active ultimate state, keyed by UUID. */
    private static final Map<UUID, UltimateState> ACTIVE = new HashMap<>();

    private UltimateAbilityManager() {}

    // -------------------------------------------------------------------------
    // Inner state record
    // -------------------------------------------------------------------------

    private static final class UltimateState {
        final AbilityType type;
        final long endTimeMs;

        UltimateState(AbilityType type, int durationTicks) {
            this.type = type;
            this.endTimeMs = System.currentTimeMillis() + (long) durationTicks * 50L;
        }

        boolean isExpired() {
            return System.currentTimeMillis() >= endTimeMs;
        }

        long remainingMs() {
            return Math.max(0L, endTimeMs - System.currentTimeMillis());
        }
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Activates an ultimate form for the player.
     *
     * @param player        the server-side player
     * @param type          the ultimate ability type being activated
     * @param durationTicks how long the form lasts in game ticks
     */
    public static void startUltimate(ServerPlayer player, AbilityType type, int durationTicks) {
        ACTIVE.put(player.getUUID(), new UltimateState(type, durationTicks));
        applyEffects(player, type, durationTicks);
        player.displayClientMessage(Component.literal(activationMessage(type)), false);
    }

    /**
     * Ends an ultimate form immediately and removes its bonuses.
     *
     * @param player  the server-side player
     * @param type    the ultimate ability type that ended
     * @param notify  whether to send the expiry message to the player
     */
    public static void stopUltimate(ServerPlayer player, AbilityType type, boolean notify) {
        ACTIVE.remove(player.getUUID());
        removeEffects(player, type);
        if (notify) {
            player.displayClientMessage(Component.literal(expirationMessage(type)), false);
        }
    }

    /**
     * Removes the active-ultimate entry for this player without touching effects
     * (e.g. on death where Minecraft already clears all effects).
     */
    public static void clearPlayer(UUID playerId) {
        ACTIVE.remove(playerId);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    /** Returns {@code true} if ANY ultimate is currently active for this player. */
    public static boolean isUltimateActive(UUID playerId) {
        UltimateState state = ACTIVE.get(playerId);
        return state != null && !state.isExpired();
    }

    /** Returns {@code true} if Demon Mode is currently active for this player. */
    public static boolean isDemonModeActive(UUID playerId) {
        UltimateState state = ACTIVE.get(playerId);
        return state != null && state.type == AbilityType.DEMON_MODE && !state.isExpired();
    }

    /** Returns {@code true} if The One is currently active for this player. */
    public static boolean isTheOneActive(UUID playerId) {
        UltimateState state = ACTIVE.get(playerId);
        return state != null && state.type == AbilityType.THE_ONE && !state.isExpired();
    }

    /**
     * Returns the remaining duration of the active ultimate in milliseconds,
     * or {@code 0} if no ultimate is active.
     */
    public static long getRemainingMs(UUID playerId) {
        UltimateState state = ACTIVE.get(playerId);
        return (state != null) ? state.remainingMs() : 0L;
    }

    // -------------------------------------------------------------------------
    // Mapping helpers (safe to call from client side – no mutable state access)
    // -------------------------------------------------------------------------

    /**
     * Returns the ultimate {@link AbilityType} associated with the given
     * character, or {@link AbilityType#NONE} if the character has no ultimate.
     */
    public static AbilityType getUltimateAbilityFor(CharacterType character) {
        return switch (character) {
            case MELIODAS -> AbilityType.DEMON_MODE;
            case ESCANOR  -> AbilityType.THE_ONE;
            default       -> AbilityType.NONE;
        };
    }

    /** Returns {@code true} if the given ability type is an ultimate. */
    public static boolean isUltimateAbility(AbilityType type) {
        return type == AbilityType.DEMON_MODE || type == AbilityType.THE_ONE;
    }

    // -------------------------------------------------------------------------
    // Server tick – expire ultimates
    // -------------------------------------------------------------------------

    /**
     * Must be called once per server tick (e.g. from
     * {@link net.minecraftforge.event.TickEvent.ServerTickEvent}).
     * Removes expired ultimates and notifies the affected players.
     */
    public static void tickAll(MinecraftServer server) {
        if (ACTIVE.isEmpty()) return;

        Iterator<Map.Entry<UUID, UltimateState>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UltimateState> entry = it.next();
            if (entry.getValue().isExpired()) {
                UUID uuid = entry.getKey();
                AbilityType type = entry.getValue().type;
                it.remove();

                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    removeEffects(player, type);
                    player.displayClientMessage(
                            Component.literal(expirationMessage(type)), false);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static void applyEffects(ServerPlayer player, AbilityType type, int durationTicks) {
        switch (type) {
            case DEMON_MODE -> {
                // Strength II: +100% attack damage via vanilla MobEffect formula (3 + 1.5 * amplifier)
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST, durationTicks, 1, false, true));
                // Speed II: +30% movement speed
                player.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SPEED, durationTicks, 1, false, true));
                // Regeneration I
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, durationTicks, 0, false, true));
                // Activation sound
                player.level().playSound(
                        null, player.blockPosition(),
                        SoundEvents.WITHER_SPAWN,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case THE_ONE -> {
                // Major damage boost (amplifier 2 = Strength III)
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_BOOST, durationTicks, 2, false, true));
                // Resistance II
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE, durationTicks, 1, false, true));
                // Activation sound
                player.level().playSound(
                        null, player.blockPosition(),
                        SoundEvents.BEACON_ACTIVATE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            default -> { /* no effects for unknown types */ }
        }
    }

    private static void removeEffects(ServerPlayer player, AbilityType type) {
        switch (type) {
            case DEMON_MODE -> {
                player.removeEffect(MobEffects.DAMAGE_BOOST);
                player.removeEffect(MobEffects.MOVEMENT_SPEED);
                player.removeEffect(MobEffects.REGENERATION);
            }
            case THE_ONE -> {
                player.removeEffect(MobEffects.DAMAGE_BOOST);
                player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            }
            default -> { /* nothing to remove */ }
        }
    }

    private static String activationMessage(AbilityType type) {
        return switch (type) {
            case DEMON_MODE -> "Demon Mode activated";
            case THE_ONE    -> "The One activated";
            default         -> "Ultimate activated";
        };
    }

    private static String expirationMessage(AbilityType type) {
        return switch (type) {
            case DEMON_MODE -> "Demon Mode faded";
            case THE_ONE    -> "The One faded";
            default         -> "Ultimate faded";
        };
    }
}
