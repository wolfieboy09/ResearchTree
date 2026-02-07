package dev.wolfieboy09.researchtree.integration.kubejs.wrappers;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.util.TickDuration;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.requirements.PlayerStageRequirement;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import dev.wolfieboy09.researchtree.requirements.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RequirementWrappers {
    public static ItemRequirement item(Item item, int amount) {
        return new ItemRequirement(item, amount);
    }

    public static ItemRequirement item(TagKey<Item> itemTag, int amount) {
        return new ItemRequirement(itemTag, amount);
    }

    public static ItemRequirement item(Item item) {
        return new ItemRequirement(item, 1);
    }

    public static ItemRequirement item(TagKey<Item> itemTag) {
        return new ItemRequirement(itemTag, 1);
    }

    public static ItemRequirement item(ItemStack stack) {
        return new ItemRequirement(stack.getItem(), stack.getCount());
    }

    public static FluidRequirement fluid(Fluid fluid, int amount) {
        return new FluidRequirement(new FluidStack(fluid, amount));
    }

    public static FluidRequirement fluid(FluidStack stack) {
        return new FluidRequirement(stack);
    }

    public static EnergyRequirement energy(int amount) {
        return new EnergyRequirement(amount);
    }

    public static AdvancementRequirement advancement(ResourceLocation advancement) {
        return new AdvancementRequirement(List.of(advancement));
    }

    public static AdvancementRequirement advancement(List<ResourceLocation> advancements) {
        return new AdvancementRequirement(List.copyOf(advancements));
    }

    public static DimensionRequirement dimension(ResourceKey<Level> dimension) {
        return new DimensionRequirement(List.of(dimension));
    }

    public static DimensionRequirement dimensions(ResourceKey<Level> dimension) {
        return new DimensionRequirement(List.of(dimension));
    }

    public static DimensionRequirement dimensions(List<ResourceKey<Level>> dimensions) {
        return new DimensionRequirement(List.copyOf(dimensions));
    }

    public static TimeRequirement time(TickDuration ticks) {
        return new TimeRequirement(ticks.intTicks());
    }

    public static PlayerStageRequirement requireStage(String stage) {
        return new PlayerStageRequirement(stage);
    }

    public static KubeRequirement custom(ResourceLocation id) {
        ResearchRequirementType<?> type = RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY.get(id);

        if (type == null) {
            throw new KubeRuntimeException("Unknown requirement type: " + id);
        }

        return (KubeRequirement) type.codec().codec().parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
    }
}