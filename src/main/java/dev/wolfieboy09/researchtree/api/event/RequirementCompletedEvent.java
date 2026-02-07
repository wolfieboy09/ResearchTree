package dev.wolfieboy09.researchtree.api.event;

import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class RequirementCompletedEvent extends PlayerEvent {
    private final ResearchNode node;
    private final ResearchRequirement<?> requirement;

    public RequirementCompletedEvent(Player player, ResearchNode node, ResearchRequirement<?> requirement) {
        super(player);
        this.node = node;
        this.requirement = requirement;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public ResearchNode getNode() {
        return this.node;
    }

    public ResearchRequirement<?> getRequirement() {
        return this.requirement;
    }
}
