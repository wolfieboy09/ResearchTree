package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.player.KubePlayerEvent;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import net.minecraft.world.entity.player.Player;

public record CategoryUnlockedEventJS (
        Player player,
        ResearchCategory category
) implements KubePlayerEvent {
    @Override
    public Player getEntity() {
        return this.player;
    }

    public ResearchCategory getCategory() {
        return this.category;
    }
}