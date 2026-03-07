package com.sevensins.character.capability;

import com.sevensins.common.data.SinType;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Concrete implementation of {@link ISinData}.
 * All state is serialised to / deserialised from NBT so it survives world saves
 * and is transmitted over the network via {@link com.sevensins.network.packet.SinDataSyncPacket}.
 */
public class SinData implements ISinData, INBTSerializable<CompoundTag> {

    private static final String KEY_ACTIVE_SIN      = "active_sin";
    private static final String KEY_SIN_LEVEL        = "sin_level";
    private static final String KEY_SIN_EXPERIENCE   = "sin_experience";
    private static final String KEY_SIN_POINTS       = "sin_points";

    private final Map<SinType, Integer> sinPoints = new EnumMap<>(SinType.class);

    @Nullable
    private SinType activeSin = null;
    private int sinLevel      = 1;
    private int sinExperience = 0;

    // -------------------------------------------------------------------------
    // ISinData – alignment
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public SinType getActiveSin() {
        return activeSin;
    }

    @Override
    public void setActiveSin(@Nullable SinType sin) {
        this.activeSin = sin;
    }

    @Override
    public boolean isAligned() {
        return activeSin != null;
    }

    // -------------------------------------------------------------------------
    // ISinData – level & experience
    // -------------------------------------------------------------------------

    @Override
    public int getSinLevel() {
        return sinLevel;
    }

    @Override
    public void setSinLevel(int level) {
        this.sinLevel = Math.max(1, level);
    }

    @Override
    public int getSinExperience() {
        return sinExperience;
    }

    @Override
    public void setSinExperience(int experience) {
        this.sinExperience = Math.max(0, experience);
    }

    @Override
    public void addSinExperience(int amount) {
        this.sinExperience = Math.max(0, this.sinExperience + amount);
    }

    // -------------------------------------------------------------------------
    // ISinData – affinity points
    // -------------------------------------------------------------------------

    @Override
    public int getSinPoints(SinType sin) {
        return sinPoints.getOrDefault(sin, 0);
    }

    @Override
    public void addSinPoints(SinType sin, int amount) {
        if (amount <= 0) return;
        sinPoints.merge(sin, amount, Integer::sum);
    }

    // -------------------------------------------------------------------------
    // ISinData – persistence
    // -------------------------------------------------------------------------

    @Override
    public void copyFrom(ISinData source) {
        this.activeSin      = source.getActiveSin();
        this.sinLevel       = source.getSinLevel();
        this.sinExperience  = source.getSinExperience();
        for (SinType sin : SinType.values()) {
            int pts = source.getSinPoints(sin);
            if (pts > 0) {
                sinPoints.put(sin, pts);
            }
        }
    }

    // -------------------------------------------------------------------------
    // INBTSerializable
    // -------------------------------------------------------------------------

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        if (activeSin != null) {
            tag.putString(KEY_ACTIVE_SIN, activeSin.getId());
        }
        tag.putInt(KEY_SIN_LEVEL, sinLevel);
        tag.putInt(KEY_SIN_EXPERIENCE, sinExperience);

        CompoundTag pointsTag = new CompoundTag();
        sinPoints.forEach((sin, pts) -> pointsTag.putInt(sin.getId(), pts));
        tag.put(KEY_SIN_POINTS, pointsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        activeSin = null;
        if (tag.contains(KEY_ACTIVE_SIN)) {
            SinType.fromId(tag.getString(KEY_ACTIVE_SIN)).ifPresent(sin -> activeSin = sin);
        }
        setSinLevel(tag.getInt(KEY_SIN_LEVEL));
        sinExperience = tag.getInt(KEY_SIN_EXPERIENCE);

        sinPoints.clear();
        if (tag.contains(KEY_SIN_POINTS)) {
            CompoundTag pointsTag = tag.getCompound(KEY_SIN_POINTS);
            for (SinType sin : SinType.values()) {
                if (pointsTag.contains(sin.getId())) {
                    sinPoints.put(sin, pointsTag.getInt(sin.getId()));
                }
            }
        }
    }
}
