package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import net.minecraft.world.entity.player.Player;

public record RequirementCompletedEventJS(
        Player player,
        ResearchNode node,
        ResearchRequirement<?> requirement
) implements KubePlayerEvent {
    @Override
    public Player getEntity() {
        return this.player;
    }

    public ResearchNode getNode() {
        return this.node;
    }

    public ResearchRequirement<?> getRequirement() {
        return this.requirement;
    }
}