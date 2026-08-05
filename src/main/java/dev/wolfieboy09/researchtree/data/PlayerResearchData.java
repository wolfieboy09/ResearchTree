package dev.wolfieboy09.researchtree.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.event.CategoryUnlockedEvent;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.config.RTServerConfig;
import dev.wolfieboy09.researchtree.core.ResearchStatus;
import dev.wolfieboy09.researchtree.core.ResearchTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerResearchData implements ResearchCategory.PlayerResearchDataAccessor {
    private final Set<ResourceLocation> completedResearch = new HashSet<>();
    private final Map<ResourceLocation, ResearchProgress> activeResearch = new HashMap<>();
    private final Set<ResourceLocation> unlockedCategories = new HashSet<>();
    private GlobalPos researchTablePos = null;

    public static final Codec<PlayerResearchData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("completed").forGetter(data -> new ArrayList<>(data.completedResearch)),
                    Codec.unboundedMap(ResourceLocation.CODEC, ResearchProgress.CODEC).fieldOf("active").forGetter(data -> data.activeResearch),
                    ResourceLocation.CODEC.listOf().fieldOf("unlocked_categories").forGetter(data -> new ArrayList<>(data.unlockedCategories)),
                    GlobalPos.CODEC.optionalFieldOf("table_pos").forGetter(data -> Optional.ofNullable(data.researchTablePos))
            ).apply(instance, (completed, active, categories, tablePos) -> {
                PlayerResearchData data = new PlayerResearchData();
                data.completedResearch.addAll(completed);
                data.activeResearch.putAll(active);
                data.unlockedCategories.addAll(categories);
                data.researchTablePos = tablePos.orElse(null);
                return data;
            })
    );

    @Override
    public boolean isCompleted(ResourceLocation researchId) {
        return completedResearch.contains(researchId);
    }

    public void completeResearch(ResourceLocation researchId, @Nullable Player player) {
        completedResearch.add(researchId);
        activeResearch.remove(researchId);
        checkCategoryUnlocks(researchId, player);
    }

    private void checkCategoryUnlocks(ResourceLocation completedResearchId, @Nullable Player player) {
        for (var category : ResearchCategoryManager.getAllCategories().values()) {
            if (category.unlockRequirements().contains(completedResearchId)) {
                if (!category.isLocked(this)) {
                    unlockCategory(category.id(), player);
                }
            }
        }
    }

    public void unlockCategory(ResourceLocation categoryId, @Nullable Player player) {
        if (!unlockedCategories.contains(categoryId)) {
            unlockedCategories.add(categoryId);

            ResearchCategory category = ResearchCategoryManager.getCategory(categoryId);
            if (category != null && player != null) {
                NeoForge.EVENT_BUS.post(new CategoryUnlockedEvent(player, category));
            }
        }
    }

    public Set<ResourceLocation> getUnlockedCategories() {
        return Collections.unmodifiableSet(unlockedCategories);
    }

    public boolean canAccessCategory(ResourceLocation categoryId) {
        ResearchCategory category = ResearchCategoryManager.getCategory(categoryId);
        if (category == null) return false;

        if (category.unlockRequirements().isEmpty()) {
            return true;
        }

        return !category.isLocked(this);
    }

    public ResearchStatus getStatus(ResourceLocation researchId, ResearchTree tree) {
        if (completedResearch.contains(researchId)) {
            return ResearchStatus.COMPLETED;
        }

        if (activeResearch.containsKey(researchId)) {
            return ResearchStatus.IN_PROGRESS;
        }

        if (tree.canUnlock(researchId, completedResearch)) {
            return ResearchStatus.AVAILABLE;
        }

        return ResearchStatus.LOCKED;
    }

    /**
     * @deprecated the {@link ResearchTree} parameter is no longer needed; nodes are resolved
     * from the live {@link ResearchNodeManager} registry instead. Use {@link #canStartResearch(ResourceLocation)}.
     */
    @Deprecated
    public boolean canStartResearch(ResourceLocation researchId, @NotNull ResearchTree tree) {
        return canStartResearch(researchId);
    }

    /**
     * Whether this player is allowed to start (or resume) research on the given node right now.
     * This checks, in order: the node exists, its prerequisites are completed, it isn't already
     * completed, and finally the configured active-research limits (global + per-category).
     * A node that is already active for this player always passes the limit checks, since
     * "starting" an already-active research is just a no-op resume.
     */
    public boolean canStartResearch(ResourceLocation researchId) {
        ResearchNode node = ResearchNodeManager.getNode(researchId);
        if (node == null) return false;

        for (ResourceLocation prereq : node.prerequisites()) {
            if (!completedResearch.contains(prereq)) {
                return false;
            }
        }

        if (completedResearch.contains(researchId)) {
            return false;
        }

        // Already active: allowed, this is just a resume, not a new slot being taken.
        if (activeResearch.containsKey(researchId)) {
            return true;
        }

        return hasFreeActiveResearchSlot(node);
    }

    /**
     * Checks the global cap (Config.GLOBAL_MAX_ACTIVE_RESEARCH) and the resolved per-category
     * cap for the given node's category against this player's current active research.
     * A cap value <= 0 means "no limit" for that particular check.
     */
    private boolean hasFreeActiveResearchSlot(ResearchNode node) {
        int globalMax = RTServerConfig.GLOBAL_MAX_ACTIVE_RESEARCH.get();
        if (globalMax > 0 && activeResearch.size() >= globalMax) {
            return false;
        }

        ResourceLocation categoryId = node.category();
        ResearchCategory category = ResearchCategoryManager.getCategory(categoryId);
        int categoryMax = category != null ? category.resolvedMaxActiveResearch() : RTServerConfig.DEFAULT_CATEGORY_MAX_ACTIVE_RESEARCH.get();

        if (categoryMax > 0) {
            long activeInCategory = activeResearch.keySet().stream()
                    .map(ResearchNodeManager::getNode)
                    .filter(Objects::nonNull)
                    .filter(active -> active.category().equals(categoryId))
                    .count();

            return activeInCategory < categoryMax;
        }

        return true;
    }

    public void startResearch(ResourceLocation researchId) {
        if (!activeResearch.containsKey(researchId)) {
            activeResearch.put(researchId, new ResearchProgress());
        }
    }

    @Nullable
    public ResearchProgress getProgress(ResourceLocation researchId) {
        return activeResearch.get(researchId);
    }

    public Map<ResourceLocation, ResearchProgress> getActiveResearch() {
        return Collections.unmodifiableMap(activeResearch);
    }

    public Set<ResourceLocation> getCompletedResearch() {
        return Collections.unmodifiableSet(completedResearch);
    }

    public void setResearchTablePos(ResourceKey<Level> dimension, BlockPos pos) {
        this.researchTablePos = GlobalPos.of(dimension, pos);
    }

    public void clearResearchTablePos() {
        this.researchTablePos = null;
    }

    @Nullable
    public GlobalPos getResearchTablePos() {
        return researchTablePos;
    }

    public boolean hasResearchTable() {
        return researchTablePos != null;
    }

    public CompoundTag save() {
        return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow();
    }

    public static PlayerResearchData load(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    @ApiStatus.Internal
    public void copyFrom(PlayerResearchData other) {
        this.completedResearch.clear();
        this.completedResearch.addAll(other.completedResearch);

        this.activeResearch.clear();
        this.activeResearch.putAll(other.activeResearch);

        this.unlockedCategories.clear();
        this.unlockedCategories.addAll(other.unlockedCategories);

        this.researchTablePos = other.researchTablePos;
    }
}