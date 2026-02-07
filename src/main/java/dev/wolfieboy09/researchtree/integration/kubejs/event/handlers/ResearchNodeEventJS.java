package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import net.minecraft.world.entity.player.Player;

public record ResearchNodeEventJS(
        Player player,
        ResearchNode node
) implements KubePlayerEvent {
    @Override
    public Player getEntity() {
        return this.player;
    }

    public ResearchNode node() {
        return this.node;
    }
}
