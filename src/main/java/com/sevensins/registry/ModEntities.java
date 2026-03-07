package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.entity.DemonCommanderEntity;
import com.sevensins.entity.GrayDemonEntity;
import com.sevensins.entity.MeliodasNpcEntity;
import com.sevensins.entity.RedDemonEntity;
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

    /** Red Demon boss — spawnable via {@code /summon seven_sins:red_demon} for testing. */
    public static final RegistryObject<EntityType<RedDemonEntity>> RED_DEMON =
            ENTITY_TYPES.register(
                    RedDemonEntity.REGISTRY_NAME,
                    () -> EntityType.Builder
                            .<RedDemonEntity>of(RedDemonEntity::new, MobCategory.MONSTER)
                            .sized(0.8f, 2.5f)
                            .build(SevenSinsMod.MODID + ":" + RedDemonEntity.REGISTRY_NAME)
            );

    /** Gray Demon boss — spawnable via {@code /summon seven_sins:gray_demon} for testing. */
    public static final RegistryObject<EntityType<GrayDemonEntity>> GRAY_DEMON =
            ENTITY_TYPES.register(
                    GrayDemonEntity.REGISTRY_NAME,
                    () -> EntityType.Builder
                            .<GrayDemonEntity>of(GrayDemonEntity::new, MobCategory.MONSTER)
                            .sized(0.9f, 2.8f)
                            .build(SevenSinsMod.MODID + ":" + GrayDemonEntity.REGISTRY_NAME)
            );

    /** Demon Commander boss — spawnable via {@code /summon seven_sins:demon_commander} for testing. */
    public static final RegistryObject<EntityType<DemonCommanderEntity>> DEMON_COMMANDER =
            ENTITY_TYPES.register(
                    DemonCommanderEntity.REGISTRY_NAME,
                    () -> EntityType.Builder
                            .<DemonCommanderEntity>of(DemonCommanderEntity::new, MobCategory.MONSTER)
                            .sized(1.0f, 3.0f)
                            .build(SevenSinsMod.MODID + ":" + DemonCommanderEntity.REGISTRY_NAME)
            );

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(MELIODAS_NPC.get(), MeliodasNpcEntity.createAttributes().build());
        event.put(RED_DEMON.get(), RedDemonEntity.createAttributes().build());
        event.put(GRAY_DEMON.get(), GrayDemonEntity.createAttributes().build());
        event.put(DEMON_COMMANDER.get(), DemonCommanderEntity.createAttributes().build());
    }
}
