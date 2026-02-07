package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.RTUtil;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.api.wrapper.GridPosition;
import dev.wolfieboy09.researchtree.data.ResearchCategoryManager;
import dev.wolfieboy09.researchtree.data.ResearchProgressData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ResearchNodeBuilder {
    private transient final ResourceLocation id;
    private transient ItemStack icon;
    private transient Component title;
    private transient Component description = Component.empty();
    private transient final List<ResourceLocation> prerequisites;
    private transient final List<ResearchRequirement<?>> requirements;
    private transient final List<ResearchReward> rewards;
    private transient GridPosition gridPos = new GridPosition(0, 0);
    @Nullable
    private transient ResearchCategory category = null;
    private transient boolean hidden = false;
    private transient ResearchProgressData progressData = ResearchProgressData.DEFAULT;
    private transient ResearchModificationEventJS parentEvent;

    @HideFromJS
    public ResearchNodeBuilder(ResourceLocation id) {
        this.id = id;
        this.icon = new ItemStack(Items.BOOK);
        String readableName = id.getPath().replace('_', ' ');
        readableName = Character.toUpperCase(readableName.charAt(0)) + readableName.substring(1);
        this.title = Component.literal(readableName);
        this.prerequisites = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.rewards = new ArrayList<>();
    }

    @HideFromJS
    public ResearchNodeBuilder(ResearchNode node) {
        this.id = node.id();
        this.icon = node.icon();
        this.title = node.title();
        this.description = node.description();
        this.prerequisites = List.copyOf(node.prerequisites());
        this.requirements = List.copyOf(node.requirements());
        this.rewards = List.copyOf(node.rewards());
        this.category = node.category().orElse(null);
        this.hidden = node.hidden();
        this.progressData = node.progressData();
    }

    @HideFromJS
    void setParentEvent(ResearchModificationEventJS event) {
        this.parentEvent = event;
    }

    public ResearchNodeBuilder title(Component title) {
        this.title = title;
        return this;
    }

    public ResearchNodeBuilder description(Component description) {
        this.description = description;
        return this;
    }

    public ResearchNodeBuilder icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    public ResearchNodeBuilder prerequisite(ResourceLocation prerequisite) {
        this.prerequisites.add(prerequisite);
        return this;
    }

    public ResearchNodeBuilder clearPrerequisites() {
        this.prerequisites.clear();
        return this;
    }

    public ResearchNodeBuilder requires(ResearchRequirement<?> requirement) {
        this.requirements.add(requirement);
        return this;
    }

    public ResearchNodeBuilder clearRequirements() {
        this.requirements.clear();
        return this;
    }

    public ResearchNodeBuilder reward(ResearchReward reward) {
        this.rewards.add(reward);
        return this;
    }

    public ResearchNodeBuilder addReward(ResearchReward reward) {
        return reward(reward);
    }

    public ResearchNodeBuilder clearRewards() {
        this.rewards.clear();
        return this;
    }

    public ResearchNodeBuilder pos(int x, int y) {
        this.gridPos = new GridPosition(x, y);
        return this;
    }


    public ResearchNodeBuilder position(int x, int y) {
        return pos(x, y);
    }

    public ResearchNodeBuilder category(ResourceLocation categoryId) {
        ResearchCategory existingCategory = ResearchCategoryManager.getCategory(categoryId);

        if (existingCategory == null) {
            throw new KubeRuntimeException("The category " + categoryId + " does not exist");
        }

        this.category = existingCategory;
        return this;
    }

    public ResearchNodeBuilder hidden() {
        this.hidden = true;
        return this;
    }

    public ResearchNodeBuilder ticksPerPercent(TickDuration ticks) {
        this.progressData = new ResearchProgressData(ticks.intTicks());
        return this;
    }

    @HideFromJS
    public ResearchNode build() {
        ResearchNode node = new ResearchNode(
                id,
                icon,
                title,
                description,
                List.copyOf(prerequisites),
                List.copyOf(requirements),
                List.copyOf(rewards),
                gridPos,
                Optional.ofNullable(category),
                hidden,
                progressData
        );

        RTUtil.callNotNull(parentEvent, (e) -> e.registerNode(id, this));

        return node;
    }

    @HideFromJS
    public ResourceLocation getId() {
        return id;
    }

    @HideFromJS
    public List<ResourceLocation> getPrerequisites() {
        return prerequisites;
    }

    @HideFromJS
    public List<ResearchRequirement<?>> getRequirements() {
        return requirements;
    }

    @HideFromJS
    public List<ResearchReward> getRewards() {
        return rewards;
    }
}