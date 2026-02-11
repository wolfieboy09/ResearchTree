package dev.wolfieboy09.researchtree.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Holds timing configuration for research progression
 */
public record ResearchProgressData(int ticksPerPercent) {
    public static final Codec<ResearchProgressData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tpp").forGetter(ResearchProgressData::ticksPerPercent)
            ).apply(instance, ResearchProgressData::new)
    );

    /**
     * Default configuration: 100 ticks (5 seconds) per 1% progress
     * Total time: 10000 ticks (500 seconds / ~8.3 minutes)
     */
    public static final ResearchProgressData DEFAULT = new ResearchProgressData(100);

    public ResearchProgressData(int ticksPerPercent) {
        this.ticksPerPercent = Math.max(1, ticksPerPercent);
    }

    /**
     * @return Number of ticks required for 1% progress (0.01 progress)
     */
    @Override
    public int ticksPerPercent() {
        return ticksPerPercent;
    }

    /**
     * @return Maximum ticks to complete the research (at 100% / 1.0 progress)
     * Automatically calculated as ticksPerPercent * 100
     */
    public int getMaxTicks() {
        return ticksPerPercent * 100;
    }

    /**
     * Calculate progress increment based on ticks passed
     *
     * @param ticksPassed Number of ticks that have passed
     * @return Progress increment (0.0 to 1.0)
     */
    public float calculateProgressIncrement(int ticksPassed) {
        return (float) ticksPassed / getMaxTicks();
    }

    /**
     * @return Estimated time in seconds to complete
     */
    public float getEstimatedTimeSeconds() {
        return getMaxTicks() / 20.0f;
    }

    /**
     * @return Estimated time in minutes to complete
     */
    public float getEstimatedTimeMinutes() {
        return getEstimatedTimeSeconds() / 60.0f;
    }

    /**
     * Check if enough ticks have passed for a progress update
     *
     * @param currentTicks Current tick counter
     * @return true if progress should be incremented
     */
    public boolean shouldIncrementProgress(int currentTicks) {
        return currentTicks >= ticksPerPercent;
    }

    @Override
    public @NotNull String toString() {
        return "ResearchProgressData{" +
                "ticksPerPercent=" + ticksPerPercent +
                ", maxTicks=" + getMaxTicks() +
                ", estimatedTime=" + String.format("%.1f", getEstimatedTimeMinutes()) + "min" +
                '}';
    }
}