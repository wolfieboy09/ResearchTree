package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RTItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> RESEARCH_TABLE =
            ITEMS.register("research_table", () ->
                    new BlockItem(
                            RTBlocks.RESEARCH_TABLE.get(),
                            new Item.Properties()
                    )
            );

    public static final DeferredItem<Item> RESEARCH_NOTE =
            ITEMS.register("research_note", () -> new Item(new Item.Properties()));
}
