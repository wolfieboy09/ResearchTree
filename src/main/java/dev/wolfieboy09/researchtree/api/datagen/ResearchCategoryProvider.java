package dev.wolfieboy09.researchtree.api.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ResearchCategoryProvider implements DataProvider {
    private final String modId;
    private final PackOutput output;
    private final List<Builder> categories = new ArrayList<>();

    public ResearchCategoryProvider(String modId, PackOutput output) {
        this.modId = modId;
        this.output = output;
    }

    protected abstract void generate();

    protected Builder category(String path) {
        return new Builder(ResourceLocation.fromNamespaceAndPath(modId, path));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        categories.clear();
        generate();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Builder builder : categories) {
            ResearchCategory category = builder.build();

            JsonElement json = ResearchCategory.CODEC
                    .encodeStart(JsonOps.INSTANCE, category)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode category " + category.id() + ": " + msg));

            // data/<namespace>/researchtree/categories/<path>.json
            Path filePath = output
                    .getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve(category.id().getNamespace())
                    .resolve("researchtree")
                    .resolve("categories")
                    .resolve(category.id().getPath() + ".json");

            if (builder.name.equals(Component.empty())) {
                builder.name = Component.translatable("category." + builder.id.toString().replace(":", "."));
            }

            futures.add(DataProvider.saveStable(cachedOutput, json, filePath));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Research Categories for mod id: " + this.modId;
    }

    public class Builder {
        private final ResourceLocation id;
        private Component name;
        private Component description = Component.empty();
        private ItemStack icon = ItemStack.EMPTY;
        private Optional<ResourceLocation> unlockRequirement = Optional.empty();
        private final List<ResourceLocation> prerequisites = new ArrayList<>();
        private int sortOrder = 0;

        private Builder(ResourceLocation id) {
            this.id = id;
            String readableName = id.getPath().replace('_', ' ');
            this.name = Component.literal(Character.toUpperCase(readableName.charAt(0)) + readableName.substring(1));
            categories.add(this);
        }

        public Builder name(Component name) {
            this.name = name;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public Builder unlockRequirement(ResourceLocation researchId) {
            this.unlockRequirement = Optional.of(researchId);
            return this;
        }

        public Builder prerequisite(ResourceLocation categoryId) {
            this.prerequisites.add(categoryId);
            return this;
        }

        public Builder sortOrder(int order) {
            this.sortOrder = order;
            return this;
        }

        ResearchCategory build() {
            return new ResearchCategory(
                    id, name, description, icon,
                    unlockRequirement, List.copyOf(prerequisites), sortOrder
            );
        }
    }
}