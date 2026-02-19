package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RTCreativeMenu {
    public static final DeferredRegister<CreativeModeTab> REGISTER =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESEARCH_TREE_STUFF =
            REGISTER.register("everything_tab", () ->
                    CreativeModeTab.builder()
                            .icon(() -> new ItemStack(RTBlocks.RESEARCH_TABLE.get()))
                            .displayItems((params, output) -> {
                                        output.accept(RTItems.RESEARCH_NOTE);
                                        output.accept(RTItems.RESEARCH_TABLE.get());
                                    }
                            )
                            .title(Component.translatable("creative_tab.researchtree"))
                            .build()
            );
}
