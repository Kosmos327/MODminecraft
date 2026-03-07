package com.sevensins.ability;

import com.sevensins.character.CharacterType;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized manager for always-on passive bonuses granted by each {@link CharacterType}.
 *
 * <p>Version 1 passive identity per character:</p>
 * <ul>
 *   <li><b>MELIODAS (Wrath)</b> – +20 % bonus damage; +5 % extra when below 50 % health.</li>
 *   <li><b>ESCANOR (Pride)</b>  – +10 % bonus damage during daytime.</li>
 *   <li><b>BAN (Greed)</b>      – 10 % lifesteal on melee damage dealt.</li>
 *   <li><b>KING (Sloth)</b>     – +15 % max mana.</li>
 *   <li><b>GOWTHER (Lust)</b>   – targets hit deal slightly less damage for a short time.</li>
 *   <li><b>DIANE (Envy)</b>     – +25 % mana regeneration rate.</li>
 *   <li><b>MERLIN (Gluttony)</b>– 10 % damage resistance.</li>
 * </ul>
 *
 * <p>All public methods are safe to call with any player or {@code null} – they return
 * sensible defaults (0 / 1.0 / {@code false}) when capabilities are missing.</p>
 *
 * <p>Optional sin-level scaling: every {@value #SCALING_LEVEL_INTERVAL} sin levels,
 * each passive grows slightly stronger up to {@value #SCALING_MAX_STACKS} extra stacks.</p>
 */
public final class PassiveAbilityManager {

    // -------------------------------------------------------------------------
    // Constants – centralised here for easy balancing
    // -------------------------------------------------------------------------

    // WRATH (MELIODAS)
    public static final float WRATH_DAMAGE_BONUS          = 0.20f;
    public static final float WRATH_LOW_HEALTH_EXTRA      = 0.05f;
    /** Health fraction threshold below which the low-health bonus activates. */
    private static final float WRATH_LOW_HEALTH_THRESHOLD = 0.50f;

    // PRIDE (ESCANOR)
    public static final float PRIDE_DAYTIME_DAMAGE_BONUS  = 0.10f;

    // GREED (BAN)
    public static final float GREED_LIFESTEAL_FRACTION    = 0.10f;

    // SLOTH (KING)
    public static final float SLOTH_MAX_MANA_BONUS        = 0.15f;

    // LUST (GOWTHER)
    public static final float LUST_DEBUFF_REDUCTION       = 0.10f;
    /** Duration in milliseconds of the Lust debuff on a struck target. */
    private static final long  LUST_DEBUFF_DURATION_MS    = 3_000L;

    // ENVY (DIANE)
    public static final float ENVY_MANA_REGEN_BONUS       = 0.25f;

    // GLUTTONY (MERLIN)
    public static final float GLUTTONY_RESISTANCE_BONUS   = 0.10f;

    // Scaling
    /** Sin levels between each passive scaling step. */
    private static final int  SCALING_LEVEL_INTERVAL      = 10;
    /** Maximum number of extra scaling stacks (e.g. 5 × interval = up to level 50). */
    private static final int  SCALING_MAX_STACKS          = 5;
    /** Per-stack bonus increment (added to the base multiplier each stack). */
    private static final float SCALING_STEP               = 0.01f;

    // -------------------------------------------------------------------------
    // Lust debuff state – maps target entity UUID → expiry timestamp (ms)
    // -------------------------------------------------------------------------

    private static final Map<UUID, Long> LUST_DEBUFFED = new ConcurrentHashMap<>();

    private PassiveAbilityManager() {}

    // =========================================================================
    // Public query API
    // =========================================================================

    /**
     * Returns {@code true} if the player's character has the specified passive active.
     * Currently all passives are active as long as the player has chosen a character.
     */
    public static boolean hasPassive(Player player, String passiveId) {
        CharacterType type = getCharacterType(player);
        if (type == CharacterType.NONE) return false;
        return passiveId != null && passiveId.equalsIgnoreCase(type.name() + "_passive");
    }

    /**
     * Returns the additive damage multiplier bonus for the player (e.g. 0.20 = +20%).
     * This bonus is applied multiplicatively to incoming base damage.
     * Returns 0 when no bonus applies.
     */
    public static float getBonusDamage(Player player) {
        CharacterType type = getCharacterType(player);
        float scale = scalingFactor(player);
        return switch (type) {
            case MELIODAS -> {
                float base = WRATH_DAMAGE_BONUS + scale;
                // Additional bonus when below 50% health
                if (player.getHealth() / player.getMaxHealth() < WRATH_LOW_HEALTH_THRESHOLD) {
                    base += WRATH_LOW_HEALTH_EXTRA;
                }
                yield base;
            }
            case ESCANOR -> {
                if (player.level().isDay()) {
                    yield PRIDE_DAYTIME_DAMAGE_BONUS + scale;
                }
                yield 0f;
            }
            default -> 0f;
        };
    }

    /**
     * Returns the mana regeneration bonus multiplier (e.g. 0.25 = +25%).
     * Returns 0 when no bonus applies.
     */
    public static float getManaRegenBonus(Player player) {
        CharacterType type = getCharacterType(player);
        float scale = scalingFactor(player);
        return switch (type) {
            case DIANE   -> ENVY_MANA_REGEN_BONUS + scale;
            default      -> 0f;
        };
    }

    /**
     * Returns the movement speed modifier bonus (e.g. 0.10 = +10%).
     * Returns 0 when no bonus applies.
     */
    public static float getSpeedModifier(Player player) {
        // Version 1: no character has a pure speed passive yet.
        return 0f;
    }

    /**
     * Returns the damage resistance fraction (e.g. 0.10 = 10% incoming damage reduction).
     * Returns 0 when no bonus applies.
     */
    public static float getResistanceBonus(Player player) {
        CharacterType type = getCharacterType(player);
        float scale = scalingFactor(player);
        return switch (type) {
            case MERLIN -> GLUTTONY_RESISTANCE_BONUS + scale;
            default     -> 0f;
        };
    }

    /**
     * Returns the mana cost reduction fraction for abilities (e.g. 0.10 = −10% cost).
     * Returns 0 when no bonus applies.
     */
    public static float getAbilityCostReduction(Player player) {
        // Version 1: no character has an ability cost reduction passive yet.
        return 0f;
    }

    /**
     * Returns the max-mana multiplier bonus for the player (e.g. 0.15 = +15% max mana).
     * For example, SLOTH (KING) returns 0.15 indicating +15% max mana.
     * Returns 0 when no bonus applies.
     */
    public static float getMaxManaBonus(Player player) {
        CharacterType type = getCharacterType(player);
        float scale = scalingFactor(player);
        return switch (type) {
            case KING -> SLOTH_MAX_MANA_BONUS + scale;
            default   -> 0f;
        };
    }

    /**
     * Returns {@code true} if the given living entity is currently under the Lust debuff
     * (i.e. was recently struck by a LUST/GOWTHER player and should deal less damage).
     */
    public static boolean isLustDebuffed(LivingEntity entity) {
        Long expiry = LUST_DEBUFFED.get(entity.getUUID());
        if (expiry == null) return false;
        if (System.currentTimeMillis() > expiry) {
            LUST_DEBUFFED.remove(entity.getUUID());
            return false;
        }
        return true;
    }

    // =========================================================================
    // Event callbacks – called from PassiveAbilityEvents
    // =========================================================================

    /**
     * Called each server-side player tick.  Handles periodic passive effects.
     *
     * <ul>
     *   <li>Cleans up expired Lust debuff entries periodically.</li>
     * </ul>
     *
     * @param player the ticking server player
     */
    public static void onPlayerTick(ServerPlayer player) {
        // Periodically purge expired Lust debuff entries (every ~5 seconds)
        if (player.tickCount % 100 == 0 && !LUST_DEBUFFED.isEmpty()) {
            long now = System.currentTimeMillis();
            LUST_DEBUFFED.entrySet().removeIf(e -> now > e.getValue());
        }
    }

    /**
     * Called when a player deals damage to a living entity.
     *
     * <ul>
     *   <li><b>GREED (BAN)</b> – heals the attacker for {@value #GREED_LIFESTEAL_FRACTION}
     *       of the damage dealt.</li>
     *   <li><b>LUST (GOWTHER)</b> – applies a short damage-reduction debuff to the target.</li>
     * </ul>
     *
     * @param attacker the player who dealt the damage
     * @param target   the entity that received the damage
     * @param amount   the damage amount dealt
     */
    public static void onDamageDealt(ServerPlayer attacker, LivingEntity target, float amount) {
        CharacterType type = getCharacterType(attacker);
        switch (type) {
            case BAN -> {
                float heal = amount * GREED_LIFESTEAL_FRACTION;
                if (heal > 0) {
                    attacker.heal(heal);
                }
            }
            case GOWTHER -> {
                // Apply Lust debuff to the target
                long expiry = System.currentTimeMillis() + LUST_DEBUFF_DURATION_MS;
                LUST_DEBUFFED.put(target.getUUID(), expiry);
            }
            default -> { /* no-op */ }
        }
    }

    /**
     * Called when a player is about to take damage.  Returns the effective damage
     * amount after applying any passive damage-reduction bonuses.
     *
     * @param player the player being hurt
     * @param amount the incoming damage amount
     * @return the potentially reduced damage amount
     */
    public static float onDamageTaken(Player player, float amount) {
        float resistance = getResistanceBonus(player);
        if (resistance > 0f) {
            amount *= (1f - resistance);
        }
        return Math.max(0f, amount);
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    /**
     * Safely retrieves the player's selected {@link CharacterType}.
     * Returns {@link CharacterType#NONE} when the capability is unavailable.
     */
    public static CharacterType getCharacterType(Player player) {
        if (player == null) return CharacterType.NONE;
        return ModCapabilities.get(player)
                .map(cap -> cap.getData().getSelectedCharacter())
                .orElse(CharacterType.NONE);
    }

    /**
     * Returns an optional flat scaling bonus based on the player's sin level.
     * Every {@value #SCALING_LEVEL_INTERVAL} sin levels the passive gets slightly stronger,
     * up to {@value #SCALING_MAX_STACKS} extra stacks of {@value #SCALING_STEP} each.
     *
     * <p>Returns 0 when the sin-data capability is unavailable.</p>
     */
    private static float scalingFactor(Player player) {
        if (player == null) return 0f;
        int sinLevel = player.getCapability(ModCapabilities.SIN_DATA)
                .map(sinData -> sinData.getSinLevel())
                .orElse(0);
        int stacks = Math.min(sinLevel / SCALING_LEVEL_INTERVAL, SCALING_MAX_STACKS);
        return stacks * SCALING_STEP;
    }
}
