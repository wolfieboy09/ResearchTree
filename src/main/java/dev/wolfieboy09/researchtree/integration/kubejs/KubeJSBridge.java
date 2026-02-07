package dev.wolfieboy09.researchtree.integration.kubejs;

import dev.latvian.mods.kubejs.script.ConsoleJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.integration.kubejs.event.RTEvents;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchCategoryBuilder;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchCategoryModificationJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchModificationEventJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchNodeBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class KubeJSBridge {
    public static void applyResearchNodeModifications(Map<ResourceLocation, ResearchNode> loaded) {
        if (RTEvents.research.hasListeners()) {
            ResearchModificationEventJS event = new ResearchModificationEventJS();

            RTEvents.research.post(ScriptType.SERVER, event);

            for (ResourceLocation id : event.getNodesToRemove()) {
                loaded.remove(id);
            }

            for (var entry : event.getNodesToModify().entrySet()) {
                ResourceLocation id = entry.getKey();

                ResearchNode existing = loaded.get(id);
                if (existing != null) {
                    entry.getValue().accept(new ResearchNodeBuilder(existing));
                }
            }

            for (var entry : event.getNodesToAdd().entrySet()) {
                if (entry.getValue() != null) {
                    loaded.put(entry.getKey(), entry.getValue().build());
                } else {
                    ConsoleJS.SERVER.warn("Attempted to create a research node but it was null");
                }
            }

            event.clearCache();
        }
    }

    public static void applyResearchCategoryModifications(Map<ResourceLocation, ResearchCategory> loaded) {
        if (RTEvents.categories.hasListeners()) {
            ResearchCategoryModificationJS event = new ResearchCategoryModificationJS();

            RTEvents.categories.post(ScriptType.SERVER, event);

            for (var entry : event.getCategoriesToModify().entrySet()) {
                ResourceLocation id = entry.getKey();
                ResearchCategory existing = loaded.get(id);
                if (existing != null) {
                    entry.getValue().accept(new ResearchCategoryBuilder(existing));
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