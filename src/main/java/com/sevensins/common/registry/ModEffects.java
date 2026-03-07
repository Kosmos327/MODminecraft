package com.sevensins.common.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.common.data.SinType;
import com.sevensins.common.effect.SinEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers all {@link MobEffect}s for the mod.
 * Each sin has one associated effect that can be applied by items, abilities, or events.
 */
public class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SevenSinsMod.MODID);

    public static final RegistryObject<MobEffect> WRATH    = EFFECTS.register("wrath",
            () -> new SinEffect(SinType.WRATH,    MobEffectCategory.BENEFICIAL, 0xFF4500));

    public static final RegistryObject<MobEffect> GREED    = EFFECTS.register("greed",
            () -> new SinEffect(SinType.GREED,    MobEffectCategory.BENEFICIAL, 0xFFD700));

    public static final RegistryObject<MobEffect> SLOTH    = EFFECTS.register("sloth",
            () -> new SinEffect(SinType.SLOTH,    MobEffectCategory.HARMFUL,    0x8B8682));

    public static final RegistryObject<MobEffect> PRIDE    = EFFECTS.register("pride",
            () -> new SinEffect(SinType.PRIDE,    MobEffectCategory.BENEFICIAL, 0xE8E8E8));

    public static final RegistryObject<MobEffect> LUST     = EFFECTS.register("lust",
            () -> new SinEffect(SinType.LUST,     MobEffectCategory.BENEFICIAL, 0xFF1493));

    public static final RegistryObject<MobEffect> ENVY     = EFFECTS.register("envy",
            () -> new SinEffect(SinType.ENVY,     MobEffectCategory.BENEFICIAL, 0x228B22));

    public static final RegistryObject<MobEffect> GLUTTONY = EFFECTS.register("gluttony",
            () -> new SinEffect(SinType.GLUTTONY, MobEffectCategory.NEUTRAL,    0xFF8C00));
}
