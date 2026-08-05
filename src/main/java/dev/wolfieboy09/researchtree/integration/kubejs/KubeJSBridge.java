package dev.wolfieboy09.researchtree.integration.kubejs;

import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.integration.kubejs.event.RTEvents;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchCategoryModificationJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchModificationEventJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchNodeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KubeJSBridge {
    public static void applyResearchNodeModifications(Map<ResourceLocation, ResearchNode> loaded) {
        if (!RTEvents.research.hasListeners()) {
            return;
        }

        Map<ResourceLocation, ResearchNode> nodesToAdd = new HashMap<>();
        Map<ResourceLocation, ResearchNode> nodesToModify = new HashMap<>();
        Set<ResourceLocation> nodesToRemove = new HashSet<>();

        Set<ResourceLocation> targets = new HashSet<>(RTEvents.research.findUniqueExtraIds(ScriptType.SERVER));
        targets.addAll(RTEvents.research.findUniqueExtraIds(ScriptType.STARTUP));

        for (ResourceLocation target : targets) {
            ResearchModificationEventJS event = new ResearchModificationEventJS(target);

            RTEvents.research.post(ScriptType.SERVER, target, event);

            collectNodeModifications(event, nodesToAdd, nodesToModify, nodesToRemove);
        }

        for (ResourceLocation id : nodesToRemove) {
            loaded.remove(id);
        }

        for (var entry : nodesToModify.entrySet()) {
            ResourceLocation id = entry.getKey();

            if (loaded.containsKey(id)) {
                loaded.put(id, entry.getValue());
            }
        }

        loaded.putAll(nodesToAdd);
    }

    private static void collectNodeModifications(ResearchModificationEventJS event,
                                                 Map<ResourceLocation, ResearchNode> nodesToAdd,
                                                 Map<ResourceLocation, ResearchNode> nodesToModify,
                                                 Set<ResourceLocation> nodesToRemove) {
        nodesToRemove.addAll(event.getNodesToRemove());

        for (var entry : event.getNodesToModify().entrySet()) {
            nodesToModify.put(entry.getKey(), entry.getValue().build());
        }

        for (var entry : event.getNodesToAdd().entrySet()) {
            ResearchNodeBuilder builder = entry.getValue();

            if (builder != null) {
                nodesToAdd.put(entry.getKey(), builder.build());
            } else {
                ConsoleJS.SERVER.warn("Attempted to create a research node but it was null");
            }
        }

        event.clearCache();
    }

    public static void applyResearchCategoryModifications(Map<ResourceLocation, ResearchCategory> loaded) {
        if (RTEvents.categories.hasListeners()) {
            ResearchCategoryModificationJS event = new ResearchCategoryModificationJS();

            RTEvents.categories.post(ScriptType.SERVER, event);

            for (var entry : event.getCategoriesToModify().entrySet()) {
                ResourceLocation id = entry.getKey();

                if (loaded.containsKey(id)) {
                    loaded.put(id, entry.getValue().build());
                }
            }

            for (var entry : event.getCategoriesToAdd().entrySet()) {
                if (entry.getValue() != null) {
                    loaded.put(entry.getKey(), entry.getValue().build());
                } else {
                    ConsoleJS.SERVER.warn("Attempted to create a research category but it was null");
                }
            }

            event.clearCache();
        }
    }
}