package dev.wolfieboy09.researchtree.api.event;

import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CategoryUnlockedEvent extends PlayerEvent {
    private final ResearchCategory category;

    public CategoryUnlockedEvent(Player player, ResearchCategory category) {
        super(player);
        this.category = category;
    }

    public Level getLevel() {
        return getEntity().level();
    }

    public ResearchCategory getCategory() {
        return this.category;
    }
}