package dev.wolfieboy09.researchtree.integration.kubejs;

import dev.latvian.mods.kubejs.script.ScriptType;
import dev.wolfieboy09.researchtree.api.event.CategoryUnlockedEvent;
import dev.wolfieboy09.researchtree.api.event.RequirementCompletedEvent;
import dev.wolfieboy09.researchtree.api.event.ResearchNodeEvent;
import dev.wolfieboy09.researchtree.integration.kubejs.event.RTEvents;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.CategoryUnlockedEventJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.RequirementCompletedEventJS;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.ResearchNodeEventJS;
import net.neoforged.bus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class KubeEventListeners {
    @SubscribeEvent
    public static void researchStarted(ResearchNodeEvent.Started event) {
        if (RTEvents.researchStarted.hasListeners(event.getNode().id())) {
            RTEvents.researchStarted.post(ScriptType.SERVER, event.getNode().id(), new ResearchNodeEventJS(event.getEntity(), event.getNode()));
        }
    }

    @SubscribeEvent
    public static void researchCompleted(ResearchNodeEvent.Completed event) {
        if (RTEvents.researchCompleted.hasListeners(event.getNode().id())) {
            RTEvents.researchCompleted.post(ScriptType.SERVER, event.getNode().id(), new ResearchNodeEventJS(event.getEntity(), event.getNode()));
        }
    }

    @SubscribeEvent
    public static void requirementCompleted(RequirementCompletedEvent event) {
        if (RTEvents.requirementCompleted.hasListeners(event.getNode().id())) {
            RTEvents.requirementCompleted.post(ScriptType.SERVER, event.getNode().id(),
                    new RequirementCompletedEventJS(event.getEntity(), event.getNode(), event.getRequirement()));
        }
    }

    @SubscribeEvent
    public static void categoryUnlocked(CategoryUnlockedEvent event) {
        if (RTEvents.categoryUnlocked.hasListeners(event.getCategory().id())) {
            RTEvents.categoryUnlocked.post(ScriptType.SERVER, event.getCategory().id(),
                    new CategoryUnlockedEventJS(event.getEntity(), event.getCategory()));
        }
    }
}
