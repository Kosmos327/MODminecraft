package com.sevensins;

import com.mojang.logging.LogUtils;
import com.sevensins.common.registry.ModBlocks;
import com.sevensins.common.registry.ModCreativeTabs;
import com.sevensins.common.registry.ModEffects;
import com.sevensins.common.registry.ModItems;
import com.sevensins.common.registry.ModSounds;
import com.sevensins.config.ModConfig;
import com.sevensins.network.ModNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

/**
 * Main entry point for the Seven Deadly Sins mod.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Registers all {@link net.minecraftforge.registries.DeferredRegister}s with the mod
 *       event bus.</li>
 *   <li>Registers the common configuration spec.</li>
 *   <li>Triggers network channel registration during common setup.</li>
 * </ul>
 *
 * <p>Event handlers ({@link com.sevensins.common.event.CapabilityEventHandler},
 * {@link com.sevensins.server.event.ServerEventHandler},
 * {@link com.sevensins.client.event.ClientEventHandler}) and
 * {@link com.sevensins.common.capability.ModCapabilities} are auto-registered via
 * {@link Mod.EventBusSubscriber} annotations on those classes.
 */
@Mod(SevenSinsMod.MODID)
public class SevenSinsMod {

    public static final String MODID = "seven_sins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SevenSinsMod(IEventBus modEventBus) {
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
