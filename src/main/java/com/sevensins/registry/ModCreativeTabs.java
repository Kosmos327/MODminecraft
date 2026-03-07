package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SevenSinsMod.MODID);

    public static final RegistryObject<CreativeModeTab> SEVEN_SINS_TAB =
            CREATIVE_MODE_TABS.register("seven_sins_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.seven_sins.seven_sins_tab"))
                            .icon(() -> ModItems.DEMON_FRAGMENT.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                output.accept(ModItems.DEMON_FRAGMENT.get());
                                output.accept(ModItems.SACRED_SCROLL.get());
                            })
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
