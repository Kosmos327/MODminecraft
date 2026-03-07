package com.sevensins.common.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.common.data.SinType;
import com.sevensins.common.item.SinEmblemItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/**
 * Registers all {@link Item}s for the mod using {@link DeferredRegister}.
 * Must be registered to the mod event bus in the main mod class.
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SevenSinsMod.MODID);

    // -------------------------------------------------------------------------
    // Sin Emblems – one per sin, consumed on use to align the player
    // -------------------------------------------------------------------------

    public static final RegistryObject<Item> WRATH_EMBLEM    = registerEmblem(SinType.WRATH);
    public static final RegistryObject<Item> GREED_EMBLEM    = registerEmblem(SinType.GREED);
    public static final RegistryObject<Item> SLOTH_EMBLEM    = registerEmblem(SinType.SLOTH);
    public static final RegistryObject<Item> PRIDE_EMBLEM    = registerEmblem(SinType.PRIDE);
    public static final RegistryObject<Item> LUST_EMBLEM     = registerEmblem(SinType.LUST);
    public static final RegistryObject<Item> ENVY_EMBLEM     = registerEmblem(SinType.ENVY);
    public static final RegistryObject<Item> GLUTTONY_EMBLEM = registerEmblem(SinType.GLUTTONY);

    /** Ordered list of all emblem items for iteration (e.g. creative tab). */
    public static final List<RegistryObject<Item>> ALL_EMBLEMS = List.of(
            WRATH_EMBLEM, GREED_EMBLEM, SLOTH_EMBLEM, PRIDE_EMBLEM,
            LUST_EMBLEM, ENVY_EMBLEM, GLUTTONY_EMBLEM
    );

    // -------------------------------------------------------------------------
    // Block items
    // -------------------------------------------------------------------------

    public static final RegistryObject<Item> SIN_ALTAR_ITEM = ITEMS.register("sin_altar",
            () -> new BlockItem(ModBlocks.SIN_ALTAR.get(), new Item.Properties()));

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static RegistryObject<Item> registerEmblem(SinType sin) {
        return ITEMS.register(sin.getId() + "_emblem",
                () -> new SinEmblemItem(sin, new Item.Properties().stacksTo(1)));
    }
}
