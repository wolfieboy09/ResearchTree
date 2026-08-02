package dev.wolfieboy09.researchtree.datagen;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.registries.RTItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RTAdvancementProvider extends AdvancementProvider {
    public RTAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        super(output, registries, existingFileHelper, List.of(new ResearchNoteGenerator(), new ResearchTableGenerator()));
    }

    @ParametersAreNonnullByDefault
    private static final class ResearchNoteGenerator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            Advancement.Builder builder = Advancement.Builder.advancement();
            builder.display(
                    new ItemStack(RTItems.RESEARCH_NOTE.get()),
                    Component.translatable("advancements.researchtree.crafted_note.title"),
                    Component.translatable("advancements.researchtree.crafted_note.description"),
                    ResourceLocation.withDefaultNamespace("amethyst_block"),
                    AdvancementType.TASK,
                    true,
                    true,
                    false
            );
            builder.addCriterion("crafted_note", InventoryChangeTrigger.TriggerInstance.hasItems(RTItems.RESEARCH_NOTE.get()));
            builder.save(saver, ResearchTreeMod.byId("crafted_note"), existingFileHelper);
        }
    }

    @ParametersAreNonnullByDefault
    private static final class ResearchTableGenerator implements AdvancementProvider.AdvancementGenerator {
        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper existingFileHelper) {
            Advancement.Builder builder = Advancement.Builder.advancement();
            builder.parent(AdvancementSubProvider.createPlaceholder("researchtree:crafted_note"));
            builder.display(
                    new ItemStack(RTItems.RESEARCH_TABLE.get()),
                    Component.translatable("advancements.researchtree.crafted_table.title"),
                    Component.translatable("advancements.researchtree.crafted_table.description"),
                    null,
                    AdvancementType.TASK,
                    true,
                    true,
                    false
            );
            builder.addCriterion("crafted_table", InventoryChangeTrigger.TriggerInstance.hasItems(RTItems.RESEARCH_TABLE.get()));
            builder.save(saver, ResearchTreeMod.byId("crafted_table"), existingFileHelper);
        }
    }
}
