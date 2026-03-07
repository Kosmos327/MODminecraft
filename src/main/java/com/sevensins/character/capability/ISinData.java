package com.sevensins.character.capability;

import com.sevensins.common.data.SinType;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;

/**
 * Capability interface that stores all sin-related data for a player.
 * All player-specific sin state is accessed through this interface.
 */
public interface ISinData {

    // -------------------------------------------------------------------------
    // Sin Alignment
    // -------------------------------------------------------------------------

    /** Returns the player's currently active sin, or {@code null} if unaligned. */
    @Nullable
    SinType getActiveSin();

    /** Sets the player's active sin alignment. Pass {@code null} to un-align. */
    void setActiveSin(@Nullable SinType sin);

    /** Returns {@code true} if the player has chosen a sin alignment. */
    boolean isAligned();

    // -------------------------------------------------------------------------
    // Sin Level & Experience
    // -------------------------------------------------------------------------

    /** Returns the player's current level within their active sin (1-based). */
    int getSinLevel();

    /** Directly sets the sin level. Values below 1 are clamped to 1. */
    void setSinLevel(int level);

    /** Returns accumulated experience toward the next sin level. */
    int getSinExperience();

    /** Directly sets the sin experience value. */
    void setSinExperience(int experience);

    /**
     * Adds (or subtracts) experience to the player's sin.
     * Does not handle level-ups automatically; callers should check thresholds.
     */
    void addSinExperience(int amount);

    // -------------------------------------------------------------------------
    // Per-Sin Affinity Points (for initial alignment decisions)
    // -------------------------------------------------------------------------

    /** Returns accumulated affinity points for the given sin. */
    int getSinPoints(SinType sin);

    /** Adds affinity points for the given sin. Negative values are ignored. */
    void addSinPoints(SinType sin, int amount);

    // -------------------------------------------------------------------------
    // Persistence helpers
    // -------------------------------------------------------------------------

    /** Copies all data from the given source (used on player clone / respawn). */
    void copyFrom(ISinData source);

    /** Serializes this data to NBT for storage or network transfer. */
    CompoundTag serializeNBT();

    /** Deserializes data from the given NBT compound. */
    void deserializeNBT(CompoundTag nbt);
}
