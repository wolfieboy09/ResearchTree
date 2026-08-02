package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.TreeLayoutDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class ResearchCategoryBuilder {
    private transient final ResourceLocation id;
    private transient Component name;
    private transient Component description;
    private transient ItemStack icon;
    private transient List<ResourceLocation> unlockRequirements = new ArrayList<>();
    private transient int sortOrder;
    private transient boolean autoLayout = true;
    private transient TreeLayoutDirection layoutDirection = TreeLayoutDirection.TOP_DOWN;
    private transient Optional<Integer> maxActiveResearch = Optional.empty();

    private ResearchCategoryModificationJS parentEvent = null;

    @HideFromJS
    public ResearchCategoryBuilder(ResourceLocation id) {
        this.id = id;
        this.icon = new ItemStack(Items.BOOK);
        String readableName = id.getPath().replace('_', ' ');
        readableName = Character.toUpperCase(readableName.charAt(0)) + readableName.substring(1);
        this.name = Component.literal(readableName);
    }

    @HideFromJS
    public ResearchCategoryBuilder(ResearchCategory category) {
        this.id = category.id();
        this.name = category.name();
        this.description = category.description();
        this.icon = category.icon();
        this.unlockRequirements = category.unlockRequirements();
        this.sortOrder = category.sortOrder();
        this.autoLayout = category.autoLayout();
        this.layoutDirection = category.layoutDirection();
        this.maxActiveResearch = category.maxActiveResearch();
    }

    public ResearchCategoryBuilder name(Component name) {
        this.name = name;
        return this;
    }

    public ResearchCategoryBuilder description(Component description) {
        this.description = description;
        return this;
    }

    public ResearchCategoryBuilder icon(Item icon) {
        this.icon = new ItemStack(icon);
        return this;
    }

    public ResearchCategoryBuilder unlockRequirement(ResourceLocation unlockRequirement) {
        this.unlockRequirements.add(unlockRequirement);
        return this;
    }

    public ResearchCategoryBuilder unlockRequirement(List<ResourceLocation> unlockRequirement) {
        this.unlockRequirements.addAll(unlockRequirement);
        return this;
    }

    public ResearchCategoryBuilder removeUnlockRequirement(ResourceLocation requirement) {
        this.unlockRequirements.remove(requirement);
        return this;
    }

    public ResearchCategoryBuilder sortOrder(int order) {
        this.sortOrder = order;
        return this;
    }

    /** Disables the automatic tree layout for this category, restoring hand-placed GridPos positioning. */
    public ResearchCategoryBuilder manualLayout() {
        this.autoLayout = false;
        return this;
    }

    public ResearchCategoryBuilder autoLayout(boolean autoLayout) {
        this.autoLayout = autoLayout;
        return this;
    }

    public ResearchCategoryBuilder layoutDirection(TreeLayoutDirection direction) {
        this.layoutDirection = direction;
        return this;
    }

    /**
     * Overrides how many research nodes a player may have actively in-progress at once within
     * this category. Pass 0 (or negative) to explicitly disable the per-category cap here,
     * regardless of the configured default. Leave unset to fall back to the configured default.
     */
    public ResearchCategoryBuilder maxActiveResearch(int max) {
        this.maxActiveResearch = Optional.of(max);
        return this;
    }

    @HideFromJS
    public void setParentEvent(ResearchCategoryModificationJS event) {
        this.parentEvent = event;
    }

    @HideFromJS
    public ResearchCategory build() {
        ResearchCategory category = new ResearchCategory(
                id, name, description, icon, List.copyOf(unlockRequirements), sortOrder, autoLayout,
                layoutDirection, maxActiveResearch
        );

        if (parentEvent != null) {
            parentEvent.registerCategory(id, this);
        }

        return category;
    }
}
