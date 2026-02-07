package dev.wolfieboy09.researchtree.integration.kubejs.event;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.wolfieboy09.researchtree.integration.kubejs.event.handlers.*;
import net.minecraft.resources.ResourceLocation;

public interface RTEvents {
    EventGroup GROUP = EventGroup.of("ResearchTree");

    TargetedEventHandler<ResourceLocation> researchStarted =
            GROUP.server("researchStarted", () -> ResearchNodeEventJS.class)
                    .requiredTarget(EventTargetType.ID);

    TargetedEventHandler<ResourceLocation> researchCompleted =
            GROUP.server("researchCompleted", () -> ResearchNodeEventJS.class)
                    .requiredTarget(EventTargetType.ID);

    TargetedEventHandler<ResourceLocation> requirementCompleted =
            GROUP.server("requirementCompleted", () -> RequirementCompletedEventJS.class)
                    .requiredTarget(EventTargetType.ID);

    TargetedEventHandler<ResourceLocation> categoryUnlocked =
            GROUP.server("categoryUnlocked", () -> CategoryUnlockedEventJS.class)
                    .requiredTarget(EventTargetType.ID);

    EventHandler research = GROUP.server("research", () -> ResearchModificationEventJS.class);

    EventHandler categories = GROUP.server("category", () -> ResearchCategoryModificationJS.class);
}