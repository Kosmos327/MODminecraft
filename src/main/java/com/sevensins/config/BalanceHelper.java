package com.sevensins.config;

import com.sevensins.ability.AbilityType;
import com.sevensins.boss.BossBalanceData;
import com.sevensins.character.CharacterStats;
import com.sevensins.character.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Centralised helper that computes the final, effective balance values used at
 * runtime after all modifiers have been applied.
 *
 * <h2>Design goals</h2>
 * <ul>
 *   <li>One place to call for each balance dimension (mana cost, cooldown,
 *       damage, boss HP/damage, max mana, regen).</li>
 *   <li>Never crashes — every method falls back to the supplied base value if
 *       any source (capability, config, sacred treasure) is unavailable.</li>
 *   <li>All multipliers are sourced from {@link ModConfig} so server operators
 *       can tune the game without editing code.</li>
 * </ul>
 *
 * <h2>Modifier application order</h2>
 * <pre>
 *   mana cost  = baseCost × globalCfg × characterModifier × sacredTreasure
 *   cooldown   = baseTicks × globalCfg × characterModifier
 *   damage     = (baseDmg × globalCfg × characterMultiplier) + sacredBonus
 *   boss HP    = baseHP × bossData.hpMult × globalCfg
 *   boss dmg   = baseDmg × bossData.dmgMult × globalCfg
 *   max mana   = (baseMana + sacredBonus) × globalCfg
 *   mana regen = baseRegen × globalCfg
 * </pre>
 */
public final class BalanceHelper {

    private static final Logger LOGGER = LogManager.getLogger(BalanceHelper.class);

    /** Mana cost is never allowed to go below this value. */
    private static final int MIN_MANA_COST = 0;

    /** Cooldown is never allowed to go below this many ticks. */
    private static final int MIN_COOLDOWN_TICKS = 1;

    private BalanceHelper() {}

    // =========================================================================
    // Mana cost
    // =========================================================================

    /**
     * Returns the effective mana cost for an ability after applying all modifiers.
     *
     * <p>Modifiers applied (in order):
     * <ol>
     *   <li>{@link ModConfig#GLOBAL_MANA_COST_MULTIPLIER}</li>
     *   <li>{@link CharacterStats#getManaCostModifier(Player)} (character / passive)</li>
     * </ol>
     * The result is clamped to {@value #MIN_MANA_COST} — mana cost is never
     * negative.
     *
     * @param player      the player using the ability
     * @param abilityType the ability being used
     * @param baseCost    the ability's base mana cost
     * @return effective mana cost (&ge; 0)
     */
    public static int getEffectiveManaCost(Player player, AbilityType abilityType, int baseCost) {
        try {
            double configMult = ModConfig.GLOBAL_MANA_COST_MULTIPLIER.get();
            double charMod = CharacterStats.getManaCostModifier(player);
            double result = baseCost * configMult * charMod;
            return Math.max(MIN_MANA_COST, (int) Math.round(result));
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getEffectiveManaCost fallback for {}: {}", abilityType, e.getMessage());
            return baseCost;
        }
    }

    // =========================================================================
    // Cooldown
    // =========================================================================

    /**
     * Returns the effective cooldown in ticks for an ability after applying all
     * modifiers.
     *
     * <p>Modifiers applied (in order):
     * <ol>
     *   <li>{@link ModConfig#GLOBAL_COOLDOWN_MULTIPLIER}</li>
     *   <li>{@link CharacterStats#getCooldownModifier(Player)} (character / passive)</li>
     * </ol>
     * The result is clamped to at least {@value #MIN_COOLDOWN_TICKS} tick.
     *
     * @param player       the player using the ability
     * @param abilityType  the ability being used
     * @param baseCooldown the ability's base cooldown in ticks
     * @return effective cooldown in ticks (&ge; 1)
     */
    public static int getEffectiveCooldownTicks(Player player, AbilityType abilityType, int baseCooldown) {
        try {
            double configMult = ModConfig.GLOBAL_COOLDOWN_MULTIPLIER.get();
            double charMod = CharacterStats.getCooldownModifier(player);
            double result = baseCooldown * configMult * charMod;
            return Math.max(MIN_COOLDOWN_TICKS, (int) Math.round(result));
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getEffectiveCooldownTicks fallback for {}: {}", abilityType, e.getMessage());
            return baseCooldown;
        }
    }

    // =========================================================================
    // Ability damage
    // =========================================================================

    /**
     * Returns the effective ability damage after applying all modifiers.
     *
     * <p>Formula:
     * <pre>
     *   finalDamage = (baseDamage
     *                  × {@link ModConfig#GLOBAL_ABILITY_DAMAGE_MULTIPLIER}
     *                  × {@link CharacterStats#getAbilityDamageMultiplier(Player)})
     *               + {@link CharacterStats#getAbilityDamageBonus(Player, AbilityType)}
     *                  × {@link ModConfig#SACRED_TREASURE_BONUS_MULTIPLIER}
     * </pre>
     * The result is clamped to 0 — damage is never negative.
     *
     * @param player      the player dealing damage
     * @param abilityType the ability being used
     * @param baseDamage  the ability's base damage
     * @return effective damage (&ge; 0)
     */
    public static float getEffectiveAbilityDamage(Player player, AbilityType abilityType, float baseDamage) {
        try {
            double configMult = ModConfig.GLOBAL_ABILITY_DAMAGE_MULTIPLIER.get();
            double charMult = CharacterStats.getAbilityDamageMultiplier(player);
            double stMult = ModConfig.SACRED_TREASURE_BONUS_MULTIPLIER.get();
            float sacredBonus = CharacterStats.getAbilityDamageBonus(player, abilityType);
            float result = (float) (baseDamage * configMult * charMult) + (float) (sacredBonus * stMult);
            return Math.max(0.0f, result);
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getEffectiveAbilityDamage fallback for {}: {}", abilityType, e.getMessage());
            return baseDamage;
        }
    }

    // =========================================================================
    // Boss HP and damage
    // =========================================================================

    /**
     * Returns the effective maximum health for a boss entity.
     *
     * <p>Formula: {@code baseHp × data.hpMultiplier × globalBossHealthMultiplier}</p>
     * The result is clamped to at least {@code 1.0}.
     *
     * @param baseHp the boss's base maximum health
     * @param data   scaling overrides (use {@link BossBalanceData#DEFAULT} for normal difficulty)
     * @return effective max health (&ge; 1.0)
     */
    public static float getBossMaxHealth(float baseHp, BossBalanceData data) {
        try {
            double configMult = ModConfig.GLOBAL_BOSS_HEALTH_MULTIPLIER.get();
            return Math.max(1.0f, baseHp * data.getHpMultiplier() * (float) configMult);
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getBossMaxHealth fallback: {}", e.getMessage());
            return Math.max(1.0f, baseHp);
        }
    }

    /**
     * Convenience overload using {@link BossBalanceData#DEFAULT}.
     *
     * @param baseHp the boss's base maximum health
     * @return effective max health (&ge; 1.0)
     */
    public static float getBossMaxHealth(float baseHp) {
        return getBossMaxHealth(baseHp, BossBalanceData.DEFAULT);
    }

    /**
     * Returns the effective attack damage for a boss entity.
     *
     * <p>Formula: {@code baseDamage × data.damageMultiplier × globalBossDamageMultiplier}</p>
     * The result is clamped to {@code 0.0}.
     *
     * @param baseDamage the boss's base attack damage
     * @param data       scaling overrides
     * @return effective attack damage (&ge; 0.0)
     */
    public static double getBossDamage(double baseDamage, BossBalanceData data) {
        try {
            double configMult = ModConfig.GLOBAL_BOSS_DAMAGE_MULTIPLIER.get();
            return Math.max(0.0, baseDamage * data.getDamageMultiplier() * configMult);
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getBossDamage fallback: {}", e.getMessage());
            return Math.max(0.0, baseDamage);
        }
    }

    /**
     * Convenience overload using {@link BossBalanceData#DEFAULT}.
     *
     * @param baseDamage the boss's base attack damage
     * @return effective attack damage (&ge; 0.0)
     */
    public static double getBossDamage(double baseDamage) {
        return getBossDamage(baseDamage, BossBalanceData.DEFAULT);
    }

    // =========================================================================
    // Max mana
    // =========================================================================

    /**
     * Returns the effective maximum mana for a player after sacred-treasure and
     * config multipliers have been applied.
     *
     * <p>Formula:
     * <pre>
     *   effectiveMax = baseMana + (sacredManaBonus × sacredTreasureBonusMultiplier)
     * </pre>
     *
     * @param player the player to query
     * @return effective maximum mana (&ge; 0)
     */
    public static int getEffectiveMaxMana(Player player) {
        try {
            int baseMana = ModCapabilities.get(player)
                    .resolve()
                    .map(cap -> cap.getData().getMaxMana())
                    .orElse(0);
            int sacredBonus = CharacterStats.getMaxManaBonus(player);
            double stMult = ModConfig.SACRED_TREASURE_BONUS_MULTIPLIER.get();
            double effectiveBonus = sacredBonus * stMult;
            return Math.max(0, (int) Math.round(baseMana + effectiveBonus));
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getEffectiveMaxMana fallback: {}", e.getMessage());
            return ModCapabilities.get(player).resolve()
                    .map(cap -> cap.getData().getMaxMana())
                    .orElse(0);
        }
    }

    // =========================================================================
    // Mana regen
    // =========================================================================

    /**
     * Returns the effective mana regeneration amount per regen tick.
     *
     * <p>The base regen is scaled by {@link ModConfig#PASSIVE_BONUS_MULTIPLIER}
     * so server operators can globally tweak regeneration speed.
     *
     * <p>Formula: {@code baseRegenAmt × passiveBonusMultiplier}</p>
     *
     * @param player       the player (reserved for future per-player modifiers)
     * @param baseRegenAmt the base mana restored per regen tick
     * @return effective mana regen amount (&ge; 0)
     */
    public static int getEffectiveManaRegen(Player player, int baseRegenAmt) {
        try {
            double configMult = ModConfig.PASSIVE_BONUS_MULTIPLIER.get();
            return Math.max(0, (int) Math.round(baseRegenAmt * configMult));
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getEffectiveManaRegen fallback: {}", e.getMessage());
            return baseRegenAmt;
        }
    }

    // =========================================================================
    // Power level
    // =========================================================================

    /**
     * Returns the player's effective Power Level, accounting for Sin Level,
     * max mana (with sacred treasure), unlocked abilities, and the sacred
     * treasure power bonus.
     *
     * <p>This delegates to {@link CharacterStats#getPowerLevel(Player)} so
     * callers can use either entry point interchangeably.</p>
     *
     * @param player the player to evaluate
     * @return computed power level (&ge; 0)
     */
    public static int getPowerLevel(Player player) {
        try {
            return CharacterStats.getPowerLevel(player);
        } catch (Exception e) {
            LOGGER.debug("BalanceHelper.getPowerLevel fallback: {}", e.getMessage());
            return 0;
        }
    }

    // =========================================================================
    // Debug logging
    // =========================================================================

    /**
     * Logs all effective balance values for {@code player} and
     * {@code abilityType} at DEBUG level.
     *
     * <p>This method is a no-op unless the DEBUG log level is enabled, so it
     * is safe to leave in production builds.</p>
     *
     * @param player      the target player
     * @param abilityType the ability to evaluate
     * @param baseMana    base mana cost from the ability
     * @param baseCooldown base cooldown ticks from the ability
     * @param baseDamage  base damage from the ability
     */
    public static void debugLog(Player player, AbilityType abilityType,
                                int baseMana, int baseCooldown, float baseDamage) {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        LOGGER.debug("[BalanceHelper] player={} ability={}  manaCost={}→{}  cooldown={}→{}  damage={}→{}  powerLevel={}",
                player.getName().getString(),
                abilityType,
                baseMana,   getEffectiveManaCost(player, abilityType, baseMana),
                baseCooldown, getEffectiveCooldownTicks(player, abilityType, baseCooldown),
                baseDamage,  getEffectiveAbilityDamage(player, abilityType, baseDamage),
                getPowerLevel(player));
    }
}
