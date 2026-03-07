package com.sevensins;

import com.sevensins.network.ModNetwork;
import com.sevensins.registry.ModBlocks;
import com.sevensins.registry.ModCreativeTabs;
import com.sevensins.registry.ModEntities;
import com.sevensins.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SevenSinsMod.MODID)
public class SevenSinsMod {

    public static final String MODID = "seven_sins";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public SevenSinsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        ModNetwork.register();

        LOGGER.info("Seven Sins Mod initialized.");
    }
}
