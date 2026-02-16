package dev.wolfieboy09.researchtree.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class TimeRequirement implements ResearchRequirement<Integer> {
    public static final MapCodec<TimeRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("ticks").forGetter(req -> req.requiredTicks)
            ).apply(instance, TimeRequirement::new)
    );

    private final int requiredTicks;
    private int currentTicks;

    public TimeRequirement(int requiredTicks) {
        this.requiredTicks = requiredTicks;
        this.currentTicks = 0;
    }

    @Override
    public boolean accepts(Integer ticks) {
        return false;
    }

    @Override
    public Integer consume(Player player, Integer ticks) {
        if (currentTicks < requiredTicks && ticks != null && ticks > 0) {
            currentTicks += ticks;
        }
        return 0;
    }

    @Override
    public Integer simulateConsume(Player player, Integer ticks) {
        return 0;
    }

    @Override
    public boolean isMet(Player player) {
        return currentTicks >= requiredTicks;
    }

    @Override
    public float getProgress(Player player) {
        return (float) currentTicks / requiredTicks;
    }

    @Override
    public @NotNull Component getDisplayText() {
        int remainingSeconds = (requiredTicks - currentTicks) / 20;
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;

        if (isMet(null)) {
            return Component.translatable("requirement.researchtree.time.complete");
        }

        return Component.translatable(
                "requirement.researchtree.time",
                minutes,
                seconds
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.TIME.get();
    }

    public void incrementTick() {
        if (currentTicks < requiredTicks) {
            currentTicks++;
        }
    }
}