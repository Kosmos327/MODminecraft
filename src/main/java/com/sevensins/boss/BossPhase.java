package com.sevensins.boss;

/**
 * Represents the active combat phase of a boss encounter.
 *
 * <p>Version 1 supports two phases:
 * <ul>
 *   <li>{@link #PHASE_1} – full health to 50 %.</li>
 *   <li>{@link #PHASE_2} – below 50 % health; boss gains increased speed.</li>
 * </ul>
 */
public enum BossPhase {
    PHASE_1,
    PHASE_2
}
