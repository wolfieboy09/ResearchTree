package dev.wolfieboy09.researchtree.requirements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FluidRequirement implements ResearchRequirement<FluidStack> {
    public static final MapCodec<FluidRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FluidStack.CODEC.fieldOf("fluid").forGetter(req -> req.required)
            ).apply(instance, FluidRequirement::new)
    );

    private final FluidStack required;
    private int currentAmount;

    public FluidRequirement(FluidStack required) {
        this.required = required.copy();
        this.currentAmount = 0;
    }

    @Override
    public boolean accepts(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, required)
                && currentAmount < required.getAmount();
    }

    @Override
    public FluidStack consume(Player player, FluidStack stack) {
        if (!accepts(stack)) {
            return stack;
        }

        int needed = required.getAmount() - currentAmount;
        int toConsume = Math.min(needed, stack.getAmount());

        currentAmount += toConsume;

        FluidStack result = stack.copy();
        result.shrink(toConsume);
        return result;
    }

    @Override
    public FluidStack simulateConsume(Player player, FluidStack stack) {
        if (!accepts(stack)) {
            return stack;
        }

        int needed = required.getAmount() - currentAmount;
        int toConsume = Math.min(needed, stack.getAmount());

        FluidStack result = stack.copy();
        result.shrink(toConsume);
        return result;
    }

    @Override
    public boolean isMet(Player player) {
        return currentAmount >= required.getAmount();
    }

    @Override
    public float getProgress(Player player) {
        return (float) currentAmount / required.getAmount();
    }

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable(
                "requirement.researchtree.fluid",
                required.getHoverName(),
                currentAmount,
                required.getAmount()
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.FLUID.get();
    }

    public FluidStack getRequired() {
        return required.copy();
    }

    public int getCurrentAmount() {
        return currentAmount;
    }
}