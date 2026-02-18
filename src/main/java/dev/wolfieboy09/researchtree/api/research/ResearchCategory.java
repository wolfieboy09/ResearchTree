package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record ResearchCategory(
        ResourceLocation id,
        Component name,
        Component description,
        ItemStack icon,
        List<ResourceLocation> unlockRequirements,
        int sortOrder
) {
    public static final Codec<ResearchCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("id", ResearchTreeMod.byId("placeholder")).forGetter(ResearchCategory::id),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ResearchCategory::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(ResearchCategory::description),
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(ResearchCategory::icon),
            ResourceLocation.CODEC.listOf().optionalFieldOf("unlock_requirement", List.of()).forGetter(ResearchCategory::unlockRequirements),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(ResearchCategory::sortOrder)
    ).apply(instance, ResearchCategory::new));


    public ResearchCategory(Component name) {
        this(
                ResearchTreeMod.byId("placeholder"),
                name,
                Component.empty(),
                ItemStack.EMPTY,
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
                List.of(),
                0
        );
    }

    public boolean isLocked(PlayerResearchDataAccessor playerData) {
        for (ResourceLocation req : unlockRequirements) {
            if (!playerData.isCompleted(req)) {
                return true;
            }
        }
        return false;
    }

    public interface PlayerResearchDataAccessor {
        boolean isCompleted(ResourceLocation researchId);
    }
}