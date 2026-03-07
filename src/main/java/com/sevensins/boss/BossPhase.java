package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Phases:
 * <ul>
 *   <li>{@link #PHASE_1}     – full health; standard behaviour.</li>
 *   <li>{@link #PHASE_2}     – typically below 70 % health; increased aggression.</li>
 *   <li>{@link #PHASE_3}     – typically below 45 % health; signature mechanics more frequent.</li>
 *   <li>{@link #ENRAGED}     – typically below 30 % health; maximum aggression.</li>
 *   <li>{@link #FINAL_PHASE} – below 15 % health; supreme aggression (Demon King only).</li>
 * </ul>
 *
 * <p>Not all bosses use every phase — simpler bosses may only use
 * {@link #PHASE_1} and {@link #PHASE_2}.</p>
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2,
    PHASE_3,
    ENRAGED,
    FINAL_PHASE
}
