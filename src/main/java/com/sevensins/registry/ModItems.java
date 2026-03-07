package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SevenSinsMod.MODID);

    public static final RegistryObject<Item> DEMON_FRAGMENT = ITEMS.register("demon_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SACRED_SCROLL = ITEMS.register("sacred_scroll",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
