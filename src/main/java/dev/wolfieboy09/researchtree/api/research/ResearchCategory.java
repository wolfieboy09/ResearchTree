package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ResearchCategory(
        ResourceLocation id,
        Component name,
        Component description,
        ItemStack icon,
        Optional<ResourceLocation> unlockRequirement,
        List<ResourceLocation> prerequisites,
        int sortOrder
) {
    public static final Codec<ResearchCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("id", ResearchTreeMod.byId("placeholder")).forGetter(ResearchCategory::id),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ResearchCategory::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(ResearchCategory::description),
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(ResearchCategory::icon),
            ResourceLocation.CODEC.optionalFieldOf("unlock_requirement").forGetter(ResearchCategory::unlockRequirement),
            ResourceLocation.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(ResearchCategory::prerequisites),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(ResearchCategory::sortOrder)
    ).apply(instance, ResearchCategory::new));


    public ResearchCategory(Component name) {
        this(
                ResearchTreeMod.byId("placeholder"),
                name,
                Component.empty(),
                ItemStack.EMPTY,
                Optional.empty(),
                List.of(),
                0
        );
    }

    public ResearchCategory(Component name, ItemStack icon) {
        this(
                ResearchTreeMod.byId("placeholder"),
                name,
                Component.empty(),
                icon,
                Optional.empty(),
                List.of(),
                0
        );
    }

    public boolean isLocked(PlayerResearchDataAccessor playerData) {
        if (unlockRequirement.isPresent() && !playerData.isCompleted(unlockRequirement.get())) {
            return true;
        }

        for (ResourceLocation prereq : prerequisites) {
            if (!playerData.isCategoryUnlocked(prereq)) {
                return true;
            }
        }

        return false;
    }

    public interface PlayerResearchDataAccessor {
        boolean isCompleted(ResourceLocation researchId);
        boolean isCategoryUnlocked(ResourceLocation categoryId);
    }
}