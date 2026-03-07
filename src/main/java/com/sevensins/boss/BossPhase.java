package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Phases progress from {@link #PHASE_1} through {@link #PHASE_2} to
 * {@link #ENRAGED} as the boss loses health.  Not every boss uses all phases;
 * unused phases are simply never set.</p>
 *
 * <ul>
 *   <li>{@link #PHASE_1}   – normal combat (full health).</li>
 *   <li>{@link #PHASE_2}   – escalated combat (configurable HP threshold).</li>
 *   <li>{@link #ENRAGED}   – final desperation phase (configurable HP threshold).</li>
 * </ul>
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2,
    ENRAGED
}
