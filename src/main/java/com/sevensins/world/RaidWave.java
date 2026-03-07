package com.sevensins.world;

/**
 * Defines the parameters of a single Night Demon Raid wave.
 *
 * <p>Each wave specifies how many enemies to spawn, which wave number
 * it is (for messages), and whether it is a boss/elite wave.</p>
 */
public final class RaidWave {

    private final int waveNumber;
    private final int spawnCount;
    private final boolean bossWave;

    /**
     * @param waveNumber the 1-based wave index shown to the player
     * @param spawnCount number of mobs to spawn for this wave
     * @param bossWave   {@code true} if this wave spawns a boss/elite encounter
     */
    public RaidWave(int waveNumber, int spawnCount, boolean bossWave) {
        this.waveNumber  = waveNumber;
        this.spawnCount  = spawnCount;
        this.bossWave    = bossWave;
    }

    /** Returns the 1-based wave index. */
    public int getWaveNumber() {
        return waveNumber;
    }

    /** Returns the number of mobs to spawn for this wave. */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     * Returns {@code true} if this wave is a boss/elite wave
     * (e.g. spawns a Mythic Red Demon instead of regular mobs).
     */
    public boolean isBossWave() {
        return bossWave;
    }
}
