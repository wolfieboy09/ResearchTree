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

public class EnergyRequirement implements ResearchRequirement<Integer> {
    public static final MapCodec<EnergyRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("amount").forGetter(req -> req.required)
            ).apply(instance, EnergyRequirement::new)
    );

    private final int required;
    private int currentAmount;

    public EnergyRequirement(int required) {
        this.required = required;
        this.currentAmount = 0;
    }

    @Override
    public boolean accepts(Integer energy) {
        return energy != null && energy > 0 && currentAmount < required;
    }

    @Override
    public Integer consume(Player player, Integer energy) {
        if (!accepts(energy)) {
            return energy;
        }

        int needed = required - currentAmount;
        int toConsume = Math.min(needed, energy);

        currentAmount += toConsume;

        return energy - toConsume;
    }

    @Override
    public Integer simulateConsume(Player player, Integer energy) {
        if (!accepts(energy)) {
            return energy;
        }

        int needed = required - currentAmount;
        int toConsume = Math.min(needed, energy);

        return energy - toConsume;
    }

    @Override
    public boolean isMet(Player player) {
        return currentAmount >= required;
    }

    @Override
    public float getProgress(Player player) {
        return (float) currentAmount / required;
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(
                "requirement.researchtree.energy",
                currentAmount,
                required
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.ENERGY.get();
    }

    public int getRequired() {
        return required;
    }

    public int getCurrentAmount() {
        return currentAmount;
    }
}