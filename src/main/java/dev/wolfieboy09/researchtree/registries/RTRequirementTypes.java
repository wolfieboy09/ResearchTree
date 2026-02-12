package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.requirements.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class RTRequirementTypes {
    public static final ResourceKey<Registry<ResearchRequirementType<?>>> REQUIREMENT_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResearchTreeMod.byId("requirement_types"));

    public static final Registry<ResearchRequirementType<?>> REQUIREMENT_TYPE_REGISTRY =
            new RegistryBuilder<>(REQUIREMENT_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    public static final DeferredRegister<ResearchRequirementType<?>> REQUIREMENT_TYPES =
            DeferredRegister.create(REQUIREMENT_TYPE_REGISTRY_KEY, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<ItemRequirement>> ITEM =
            REQUIREMENT_TYPES.register("item", () -> new ResearchRequirementType<>(ItemRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<FluidRequirement>> FLUID =
            REQUIREMENT_TYPES.register("fluid", () -> new ResearchRequirementType<>(FluidRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<EnergyRequirement>> ENERGY =
            REQUIREMENT_TYPES.register("energy", () -> new ResearchRequirementType<>(EnergyRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<TimeRequirement>> TIME =
            REQUIREMENT_TYPES.register("time", () -> new ResearchRequirementType<>(TimeRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<DimensionRequirement>> DIMENSION =
            REQUIREMENT_TYPES.register("dimension", () -> new ResearchRequirementType<>(DimensionRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<TimedDimensionRequirement>> TIMED_DIMENSION =
            REQUIREMENT_TYPES.register("timed_dimension", () -> new ResearchRequirementType<>(TimedDimensionRequirement.CODEC));

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<AdvancementRequirement>> ADVANCEMENT =
            REQUIREMENT_TYPES.register("advancement", () -> new ResearchRequirementType<>(AdvancementRequirement.CODEC));
}