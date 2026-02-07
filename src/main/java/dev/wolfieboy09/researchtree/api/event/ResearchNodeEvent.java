package dev.wolfieboy09.researchtree.api.event;

import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class ResearchNodeEvent extends PlayerEvent {
    private final ResearchNode node;

    public ResearchNodeEvent(Player player, ResearchNode node) {
        super(player);
        this.node = node;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public ResearchNode getNode() {
        return this.node;
    }

    /**
     * Posted when research has started
     */
    public static class Started extends ResearchNodeEvent {
        public Started(Player player, ResearchNode node) {
            super(player, node);
        }
    }

    /**
     * Posted when research has completed
     */
    public static class Completed extends ResearchNodeEvent {
        public Completed(Player player, ResearchNode node) {
            super(player, node);
        }
    }
}
