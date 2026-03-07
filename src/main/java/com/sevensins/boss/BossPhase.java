package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Version 1 phases:
 * <ul>
 *   <li>{@link #PHASE_1} – full health; standard behaviour.</li>
 *   <li>{@link #PHASE_2} – typically below 70 % health; increased aggression.</li>
 *   <li>{@link #ENRAGED} – typically below 30 % health; maximum aggression.</li>
 * </ul>
 *
 * <p>Not all bosses use every phase — simpler bosses may only use
 * {@link #PHASE_1} and {@link #PHASE_2}.</p>
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2,
    ENRAGED
}
