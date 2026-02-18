package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class ResearchCategoryBuilder {
    private transient final ResourceLocation id;
    private transient Component name;
    private transient Component description;
    private transient ItemStack icon;
    private transient List<ResourceLocation> unlockRequirements;
    private transient int sortOrder;

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

    @HideFromJS
    public void setParentEvent(ResearchCategoryModificationJS event) {
        this.parentEvent = event;
    }

    @HideFromJS
    public ResearchCategory build() {
        ResearchCategory category = new ResearchCategory(id, name, description, icon, List.copyOf(unlockRequirements), sortOrder);

        if (parentEvent != null) {
            parentEvent.registerCategory(id, this);
        }

        return category;
    }
}
