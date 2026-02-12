package dev.wolfieboy09.researchtree.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimedDimensionRequirement extends DimensionRequirement {
    public static final MapCodec<TimedDimensionRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceKey.codec(Registries.DIMENSION).listOf()
                            .fieldOf("dimensions").forGetter(TimedDimensionRequirement::getRequiredDimensions),
                    Codec.INT.fieldOf("duration").forGetter(req -> req.duration)
            ).apply(instance, TimedDimensionRequirement::new)
    );

    private final int duration;
    private final Map<ResourceKey<Level>, Long> visitStartTimes = new HashMap<>();
    private final Map<ResourceKey<Level>, Long> totalTimeSpent = new HashMap<>();

    public TimedDimensionRequirement(List<ResourceKey<Level>> requiredDimensions, int duration) {
        super(requiredDimensions);
        this.duration = duration;
    }


    @Override
    public boolean accepts(ResourceKey<Level> dimension) {
        return getRequiredDimensions().contains(dimension) &&
                totalTimeSpent.getOrDefault(dimension, 0L) < duration;
    }

    @Override
    public ResourceKey<Level> consume(Player player, ResourceKey<Level> dimension) {
        if (!getRequiredDimensions().contains(dimension)) {
            return dimension;
        }

        long currentTime = player.level().getGameTime();

        if (!visitStartTimes.containsKey(dimension)) {
            visitStartTimes.put(dimension, currentTime);
        } else {
            long startTime = visitStartTimes.get(dimension);
            long timeSpent = currentTime - startTime;

            long currentTotal = totalTimeSpent.getOrDefault(dimension, 0L);
            long newTotal = currentTotal + timeSpent;

            totalTimeSpent.put(dimension, Math.min(newTotal, duration));

            visitStartTimes.put(dimension, currentTime);
        }

        return dimension;
    }

    @Override
    public boolean isMet(Player player) {
        for (ResourceKey<Level> dimension : getRequiredDimensions()) {
            if (totalTimeSpent.getOrDefault(dimension, 0L) < duration) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getProgress(Player player) {
        long totalRequired = (long) getRequiredDimensions().size() * duration;
        long totalAchieved = 0;

        for (ResourceKey<Level> dimension : getRequiredDimensions()) {
            totalAchieved += totalTimeSpent.getOrDefault(dimension, 0L);
        }

        return (float) totalAchieved / totalRequired;
    }

    @Override
    public Component getDisplayText() {
        int completedDimensions = 0;
        for (ResourceKey<Level> dimension : getRequiredDimensions()) {
            if (totalTimeSpent.getOrDefault(dimension, 0L) >= duration) {
                completedDimensions++;
            }
        }

        return Component.translatable(
                "requirement.researchtree.timed_dimension",
                completedDimensions,
                getRequiredDimensions().size(),
                duration / 20
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.TIMED_DIMENSION.get();
    }
}