package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.wrapper.GridPosition;
import dev.wolfieboy09.researchtree.data.ResearchProgressData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ResearchNode(
        ResourceLocation id,
        ItemStack icon,
        Component title,
        Component description,
        List<ResourceLocation> prerequisites,
        List<ResearchRequirement<?>> requirements,
        List<ResearchReward> rewards,
        GridPosition gridPos,
        Optional<ResearchCategory> category,
        boolean hidden,
        ResearchProgressData progressData
) {
    public static final Codec<ResearchNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // The ID is set from the ResearchNodeManager when going from file to node
            // ID is set from the namespace and file name
            ResourceLocation.CODEC.optionalFieldOf("id", ResearchTreeMod.byId("placeholder")).forGetter(ResearchNode::id),
            ItemStack.SINGLE_ITEM_CODEC.fieldOf("icon").forGetter(ResearchNode::icon),
            ComponentSerialization.CODEC.fieldOf("title").forGetter(ResearchNode::title),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(ResearchNode::description),
            ResourceLocation.CODEC.listOf().optionalFieldOf("prerequisites", List.of()).forGetter(ResearchNode::prerequisites),
            ResearchRequirement.DISPATCH_CODEC.listOf().optionalFieldOf("requirements", List.of()).forGetter(ResearchNode::requirements),
            ResearchReward.DISPATCH_CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(ResearchNode::rewards),
            GridPosition.CODEC.fieldOf("pos").forGetter(ResearchNode::gridPos),
            ResearchCategory.CODEC.optionalFieldOf("category").forGetter(ResearchNode::category),
            Codec.BOOL.optionalFieldOf("hidden", false).forGetter(ResearchNode::hidden),
            ResearchProgressData.CODEC.optionalFieldOf("progress_data", ResearchProgressData.DEFAULT).forGetter(ResearchNode::progressData)
    ).apply(instance, ResearchNode::new));
}