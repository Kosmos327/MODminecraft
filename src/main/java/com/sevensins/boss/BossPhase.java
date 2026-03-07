package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Standard two-phase bosses use {@link #PHASE_1} and {@link #PHASE_2}.
 * The Demon King final boss additionally uses {@link #PHASE_3} and
 * {@link #FINAL_PHASE}.</p>
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2,
    /** Demon King phase — begins at 45 % HP. */
    PHASE_3,
    /** Demon King enraged phase — begins at 15 % HP. */
    FINAL_PHASE
}
