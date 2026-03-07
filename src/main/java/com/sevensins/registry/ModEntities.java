package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.entity.MeliodasNpcEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SevenSinsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SevenSinsMod.MODID);

    /** Meliodas mentor NPC — spawnable via /summon or in creative mode. */
    public static final RegistryObject<EntityType<MeliodasNpcEntity>> MELIODAS_NPC =
            ENTITY_TYPES.register(
                    MeliodasNpcEntity.REGISTRY_NAME,
                    () -> EntityType.Builder
                            .<MeliodasNpcEntity>of(MeliodasNpcEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f)
                            .build(SevenSinsMod.MODID + ":" + MeliodasNpcEntity.REGISTRY_NAME)
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(MELIODAS_NPC.get(), MeliodasNpcEntity.createAttributes().build());
    }
}
