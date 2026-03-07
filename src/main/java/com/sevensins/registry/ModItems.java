package com.sevensins.registry;

import com.sevensins.SevenSinsMod;
import com.sevensins.common.data.SinType;
import com.sevensins.item.ChastiefolItem;
import com.sevensins.item.LostvayneItem;
import com.sevensins.item.MagicScrollItem;
import com.sevensins.item.RhittaItem;
import com.sevensins.item.SinEmblemItem;
import com.sevensins.item.SinFragmentItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
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
    // Dungeon reward items
    // -------------------------------------------------------------------------

    /** Sin Fragment — dropped as a dungeon reward; progression currency. */
    public static final RegistryObject<Item> SIN_FRAGMENT = ITEMS.register("sin_fragment",
            () -> new SinFragmentItem(new Item.Properties()));

    /** Magic Scroll — chance-based dungeon reward. */
    public static final RegistryObject<Item> MAGIC_SCROLL = ITEMS.register("magic_scroll",
            () -> new MagicScrollItem(new Item.Properties()));

    // -------------------------------------------------------------------------
    // General items (legacy names kept for backwards compatibility)
    // -------------------------------------------------------------------------

    public static final RegistryObject<Item> DEMON_FRAGMENT = ITEMS.register("demon_fragment",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> SACRED_SCROLL = ITEMS.register("sacred_scroll",
            () -> new Item(new Item.Properties()));

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
    // Sacred Treasures — legendary weapons linked to specific character types
    // -------------------------------------------------------------------------

    /** Lostvayne — Sacred Treasure of Meliodas (Wrath). */
    public static final RegistryObject<Item> LOSTVAYNE = ITEMS.register("lostvayne",
            () -> new LostvayneItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Divine Axe Rhitta — Sacred Treasure of Escanor (Pride). */
    public static final RegistryObject<Item> RHITTA = ITEMS.register("rhitta",
            () -> new RhittaItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Spirit Spear Chastiefol — Sacred Treasure of King (Sloth). */
    public static final RegistryObject<Item> CHASTIEFOL = ITEMS.register("chastiefol",
            () -> new ChastiefolItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    /** Ordered list of all sacred treasure items (e.g. for creative tab). */
    public static final List<RegistryObject<Item>> ALL_SACRED_TREASURES = List.of(
            LOSTVAYNE, RHITTA, CHASTIEFOL
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

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
