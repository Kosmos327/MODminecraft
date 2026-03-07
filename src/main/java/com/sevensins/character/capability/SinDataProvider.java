package com.sevensins.character.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability provider that attaches {@link ISinData} to a player entity.
 * Registered via {@link com.sevensins.event.CapabilityEventHandler}.
 */
public class SinDataProvider implements ICapabilitySerializable<CompoundTag> {

    /** Unique key used in {@link net.minecraftforge.event.AttachCapabilitiesEvent}. */
    public static final ResourceLocation ID =
            new ResourceLocation("seven_sins", "sin_data");

    private final SinData data = new SinData();
    private final LazyOptional<ISinData> optional = LazyOptional.of(() -> data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap,
                                                      @Nullable Direction side) {
        return ModCapabilities.SIN_DATA.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }

    /** Invalidates the {@link LazyOptional} when the provider is no longer needed. */
    public void invalidate() {
        optional.invalidate();
    }
}
