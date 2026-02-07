package dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.requirements;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RTButKubeRequirementRegistry {
    public static final DeferredRegister<ResearchRequirementType<?>> KUBE_REQUIREMENT_TYPES =
            DeferredRegister.create(RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY_KEY, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<ResearchRequirementType<?>, ResearchRequirementType<PlayerStageRequirement>> STAGE_UNLOCKED =
            KUBE_REQUIREMENT_TYPES.register("stage_unlocked", () -> new ResearchRequirementType<>(PlayerStageRequirement.CODEC));
}
