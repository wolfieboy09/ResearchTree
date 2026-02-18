package dev.wolfieboy09.researchtree.api.datagen;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.api.wrapper.GridPosition;
import dev.wolfieboy09.researchtree.data.ResearchProgressData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ResearchNodeProvider implements DataProvider {
    private final String modId;
    private final PackOutput output;
    private final List<Builder> nodes = new ArrayList<>();

    public ResearchNodeProvider(String modId, PackOutput output) {
        this.modId = modId;
        this.output = output;
    }

    protected abstract void generate();

    protected Builder node(String path) {
        return new Builder(ResourceLocation.fromNamespaceAndPath(modId, path));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        nodes.clear();
        generate();

        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Builder builder : nodes) {
            ResearchNode node = builder.build();

            JsonElement json = ResearchNode.CODEC
                    .encodeStart(JsonOps.INSTANCE, node)
                    .getOrThrow(msg -> new IllegalStateException("Failed to encode node " + node.id() + ": " + msg));

            String nodePath = node.id().getPath();
            String categoryFolder = node.category()
                    .map(cat -> cat.getPath() + "/")
                    .orElse("");

            // data/<namespace>/researchtree/research/<path>.json
            Path filePath = output
                    .getOutputFolder(PackOutput.Target.DATA_PACK)
                    .resolve(node.id().getNamespace())
                    .resolve("researchtree")
                    .resolve("research")
                    .resolve(categoryFolder + nodePath + ".json");

            if (builder.title.equals(Component.empty())) {
                builder.title = Component.translatable("researchnode." + builder.id.toString().replace(":", "."));
            }

            futures.add(DataProvider.saveStable(cachedOutput, json, filePath));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Research Nodes for mod id: " + this.modId;
    }

    public class Builder {
        private final ResourceLocation id;
        private ItemStack icon = ItemStack.EMPTY;
        private Component title = Component.empty();
        private Component description = Component.empty();
        private final List<ResourceLocation> prerequisites = new ArrayList<>();
        private final List<ResearchRequirement<?>> requirements = new ArrayList<>();
        private final List<ResearchReward> rewards = new ArrayList<>();
        private GridPosition gridPos = new GridPosition(0, 0);
        private Optional<ResourceLocation> category = Optional.empty();
        private boolean hidden = false;
        private ResearchProgressData progressData = ResearchProgressData.DEFAULT;

        private Builder(ResourceLocation id) {
            this.id = id;
            nodes.add(this);
        }

        public Builder icon(ItemStack icon) {
            this.icon = icon;
            return this;
        }

        public Builder icon(ItemLike icon) {
            return icon(new ItemStack(icon));
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder description(Component description) {
            this.description = description;
            return this;
        }

        public Builder prerequisite(ResourceLocation prereq) {
            this.prerequisites.add(prereq);
            return this;
        }

        public Builder prerequisite(List<ResourceLocation> prereq) {
            this.prerequisites.addAll(prereq);
            return this;
        }

        public Builder requires(ResearchRequirement<?> req) {
            this.requirements.add(req);
            return this;
        }

        public Builder reward(ResearchReward reward) {
            this.rewards.add(reward);
            return this;
        }

        public Builder pos(int x, int y) {
            this.gridPos = new GridPosition(x, y);
            return this;
        }

        public Builder category(ResourceLocation categoryId) {
            this.category = Optional.of(categoryId);
            return this;
        }

        public Builder hidden() {
            this.hidden = true;
            return this;
        }

        public Builder ticksPerPercent(int ticks) {
            this.progressData = new ResearchProgressData(ticks);
            return this;
        }

        public Builder tpp(int ticks) {
            return ticksPerPercent(ticks);
        }

        ResearchNode build() {
            return new ResearchNode(
                    id, icon, title, description,
                    List.copyOf(prerequisites), List.copyOf(requirements),
                    List.copyOf(rewards), gridPos, category, hidden, progressData
            );
        }
    }
}