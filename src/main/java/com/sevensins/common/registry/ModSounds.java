package com.sevensins.common.registry;

import com.sevensins.SevenSinsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers all {@link SoundEvent}s for the mod.
 * Actual sound files must be placed in {@code assets/seven_sins/sounds/} and listed
 * in {@code assets/seven_sins/sounds.json}.
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SevenSinsMod.MODID);

    /** Played when a player aligns with a sin by using a Sin Emblem. */
    public static final RegistryObject<SoundEvent> SIN_ALIGN =
            registerSound("sin_align");

    /** Played when a player's sin level increases. */
    public static final RegistryObject<SoundEvent> SIN_LEVEL_UP =
            registerSound("sin_level_up");

    /** Played when a player interacts with the Sin Altar. */
    public static final RegistryObject<SoundEvent> ALTAR_ACTIVATE =
            registerSound("altar_activate");

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () ->
                SoundEvent.createVariableRangeEvent(
                        new ResourceLocation(SevenSinsMod.MODID, name)));
    }
}
