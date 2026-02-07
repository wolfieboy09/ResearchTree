package dev.wolfieboy09.researchtree.requirements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DimensionRequirement implements ResearchRequirement<ResourceKey<Level>> {
    public static final MapCodec<DimensionRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceKey.codec(Registries.DIMENSION).listOf()
                            .fieldOf("dimensions").forGetter(req -> req.requiredDimensions)
            ).apply(instance, DimensionRequirement::new)
    );

    private final List<ResourceKey<Level>> requiredDimensions;
    private final Set<ResourceKey<Level>> visitedDimensions = new HashSet<>();

    public DimensionRequirement(List<ResourceKey<Level>> requiredDimensions) {
        this.requiredDimensions = requiredDimensions;
    }

    @Override
    public boolean accepts(ResourceKey<Level> dimension) {
        return requiredDimensions.contains(dimension) && !visitedDimensions.contains(dimension);
    }

    @Override
    public ResourceKey<Level> consume(Player player, ResourceKey<Level> dimension) {
        if (accepts(dimension)) {
            visitedDimensions.add(dimension);
        }
        return dimension;
    }

    @Override
    public boolean isMet(Player player) {
        return visitedDimensions.containsAll(requiredDimensions);
    }

    @Override
    public float getProgress(Player player) {
        return (float) visitedDimensions.size() / requiredDimensions.size();
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(
                "requirement.researchtree.dimension",
                visitedDimensions.size(),
                requiredDimensions.size()
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.DIMENSION.get();
    }
}