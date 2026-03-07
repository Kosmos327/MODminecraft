package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registers the mod's creative mode inventory tab.
 */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SevenSinsMod.MODID);

    public static final RegistryObject<CreativeModeTab> SEVEN_SINS_TAB =
            CREATIVE_MODE_TABS.register("seven_sins", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.seven_sins.main"))
                            .icon(() -> ModItems.WRATH_EMBLEM.get().getDefaultInstance())
                            .displayItems((parameters, output) -> {
                                ModItems.ALL_EMBLEMS.forEach(item -> output.accept(item.get()));
                                output.accept(ModItems.SIN_ALTAR_ITEM.get());
                                output.accept(ModItems.SACRED_FORGE_ITEM.get());
                                output.accept(ModItems.DEMON_FRAGMENT.get());
                                output.accept(ModItems.SACRED_SCROLL.get());
                                output.accept(ModItems.SIN_FRAGMENT.get());
                                output.accept(ModItems.MAGIC_SCROLL.get());
                                ModItems.ALL_SACRED_TREASURES.forEach(item -> output.accept(item.get()));
                            })
                            .build()
            );
}
