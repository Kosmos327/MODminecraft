package com.sevensins;

import com.mojang.logging.LogUtils;
import com.sevensins.config.ModConfig;
import com.sevensins.network.ModNetwork;
import com.sevensins.registry.ModBlocks;
import com.sevensins.registry.ModCreativeTabs;
import com.sevensins.registry.ModEffects;
import com.sevensins.registry.ModItems;
import com.sevensins.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SevenSinsMod.MODID)
public class SevenSinsMod {

    public static final String MODID = "seven_sins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SevenSinsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);

        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModEffects.EFFECTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
        LOGGER.info("[SevenSins] Common setup complete.");
    }
}