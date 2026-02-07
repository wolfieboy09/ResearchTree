package dev.wolfieboy09.researchtree.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.integration.kubejs.event.RTEvents;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.RequirementTypeBuilder;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.RewardTypeBuilder;
import dev.wolfieboy09.researchtree.integration.kubejs.wrappers.RequirementWrappers;
import dev.wolfieboy09.researchtree.integration.kubejs.wrappers.RewardWrappers;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ResearchTreeKubePlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(RTEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("Requirements", RequirementWrappers.class);

        bindings.add("Rewards", RewardWrappers.class);
    }

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY_KEY, reg -> {
            reg.addDefault(RequirementTypeBuilder.class, RequirementTypeBuilder::new);
            reg.add(ResearchTreeMod.byId("requirement_types"), RequirementTypeBuilder.class, RequirementTypeBuilder::new);
        });

        registry.of(RTRewardTypes.REWARD_TYPE_REGISTRY_KEY, reg -> {
            reg.addDefault(RewardTypeBuilder.class, RewardTypeBuilder::new);
            reg.add(ResearchTreeMod.byId("reward_types"), RewardTypeBuilder.class, RewardTypeBuilder::new);
        });
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.deny("dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies");
    }
}
