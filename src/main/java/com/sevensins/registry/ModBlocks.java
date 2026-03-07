package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.block.SinAltarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, SevenSinsMod.MODID);

    /**
     * The Sin Altar – the central interaction block for the sin alignment system.
     * Crafted from obsidian; very durable and slightly emissive.
     */
    public static final RegistryObject<Block> SIN_ALTAR = BLOCKS.register("sin_altar",
            () -> new SinAltarBlock(
                    BlockBehaviour.Properties.copy(Blocks.OBSIDIAN)
                            .strength(3.5f, 3000.0f)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> 7)
            ));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
