package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SevenSinsMod.MODID);

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
