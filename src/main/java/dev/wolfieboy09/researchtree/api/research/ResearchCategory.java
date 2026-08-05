package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.config.RTServerConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public record ResearchCategory(
        ResourceLocation id,
        Component name,
        Component description,
        ItemStack icon,
        List<ResourceLocation> unlockRequirements,
        int sortOrder,
        boolean autoLayout,
        // Overrides Config.DEFAULT_CATEGORY_MAX_ACTIVE_RESEARCH for this category specifically.
        // Empty = use the configured default. <= 0 = no per-category cap for this category.
        Optional<Integer> maxActiveResearch
) {
    public static final Codec<ResearchCategory> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("id", ResearchTreeMod.byId("placeholder")).forGetter(ResearchCategory::id),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ResearchCategory::name),
            ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(ResearchCategory::description),
            ItemStack.SINGLE_ITEM_CODEC.optionalFieldOf("icon", ItemStack.EMPTY).forGetter(ResearchCategory::icon),
            ResourceLocation.CODEC.listOf().optionalFieldOf("unlock_requirement", List.of()).forGetter(ResearchCategory::unlockRequirements),
            Codec.INT.optionalFieldOf("sort_order", 0).forGetter(ResearchCategory::sortOrder),
            // When true (default), node positions are computed automatically from the prerequisite graph
            // and any "pos" set on individual nodes is ignored. Set too false to keep hand-placed GridPos layout.
            Codec.BOOL.optionalFieldOf("auto_layout", true).forGetter(ResearchCategory::autoLayout),
            Codec.INT.optionalFieldOf("max_active_research").forGetter(ResearchCategory::maxActiveResearch)
    ).apply(instance, ResearchCategory::new));


    public ResearchCategory(Component name) {
        this(
                ResearchTreeMod.byId("placeholder"),
                name,
                Component.empty(),
                ItemStack.EMPTY,
                List.of(),
                0,
                true,
                Optional.empty()
        );
    }

    public ResearchCategory(Component name, ItemStack icon) {
        this(
                ResearchTreeMod.byId("placeholder"),
                name,
                Component.empty(),
                icon,
                List.of(),
                0,
                true,
                Optional.empty()
        );
    }

    /**
     * Resolves the effective max-active-research cap for this category:
     * this category's own override if set, otherwise the configured default.
     * A value &lt;= 0 means "no per-category cap" (only the global cap, if any, applies).
     */
    public int resolvedMaxActiveResearch() {
        return maxActiveResearch.orElseGet(RTServerConfig.DEFAULT_CATEGORY_MAX_ACTIVE_RESEARCH);
    }

    public boolean isLocked(PlayerResearchDataAccessor playerData) {
        for (ResourceLocation req : unlockRequirements) {
            if (!playerData.isCompleted(req)) {
                return true;
            }
        }
        return false;
    }

    public interface PlayerResearchDataAccessor {
        boolean isCompleted(ResourceLocation researchId);
    }
}