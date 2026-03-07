package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Supported phases:
 * <ul>
 *   <li>{@link #PHASE_1} – normal combat (full health to phase threshold).</li>
 *   <li>{@link #PHASE_2} – heightened aggression (below phase-2 threshold).</li>
 *   <li>{@link #ENRAGED} – maximum aggression (below enraged threshold).</li>
 * </ul>
 *
 * <p>Not every boss uses all three phases.  Bosses that only support two
 * phases simply never transition to {@link #ENRAGED}.
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2,
    ENRAGED
}
