package com.sevensins.boss;

/**
 * Stores the HP and damage scaling multipliers for a boss encounter.
 *
 * <p>Use {@link #DEFAULT} when no special scaling is needed. Custom instances
 * can represent mythic, raid, or story-scaled variants.</p>
 */
public final class BossBalanceData {

    /** Neutral instance: no extra scaling applied on top of config multipliers. */
    public static final BossBalanceData DEFAULT = new BossBalanceData(1.0f, 1.0f);

    /** Mythic-difficulty preset: bosses have more HP and deal more damage. */
    public static final BossBalanceData MYTHIC = new BossBalanceData(2.0f, 1.5f);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    /**
     * Multiplier applied to the boss's base maximum health.
     * Values greater than {@code 1.0} increase HP; values less than {@code 1.0}
     * decrease it.
     */
    private final float hpMultiplier;

    /**
     * Multiplier applied to the boss's base attack damage.
     * Values greater than {@code 1.0} increase damage; values less than
     * {@code 1.0} decrease it.
     */
    private final float damageMultiplier;

    /**
     * Constructs a {@link BossBalanceData} with the given multipliers.
     *
     * @param hpMultiplier     HP scaling factor (must be &gt; 0)
     * @param damageMultiplier damage scaling factor (must be &ge; 0)
     */
    public BossBalanceData(float hpMultiplier, float damageMultiplier) {
        this.hpMultiplier = Math.max(0.01f, hpMultiplier);
        this.damageMultiplier = Math.max(0.0f, damageMultiplier);
    }

    /** Returns the HP scaling multiplier for this boss variant. */
    public float getHpMultiplier() {
        return hpMultiplier;
    }

    /** Returns the damage scaling multiplier for this boss variant. */
    public float getDamageMultiplier() {
        return damageMultiplier;
    }
}
