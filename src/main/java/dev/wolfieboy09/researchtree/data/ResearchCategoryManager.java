package dev.wolfieboy09.researchtree.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.integration.kubejs.KubeJSBridge;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ResearchCategoryManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<ResourceLocation, ResearchCategory> CATEGORIES = Map.of();

    private static final Comparator<ResearchCategory> CATEGORY_ORDER =
            Comparator.comparingInt(ResearchCategory::sortOrder)
                    .thenComparing(cat -> cat.name().getString());

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonObject> jsons = new HashMap<>();
        AtomicInteger failed = new AtomicInteger();

        resourceManager.listResources("researchtree/categories", path -> path.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    String namespace = fileId.getNamespace();
                    String path = fileId.getPath();

                    if (!path.startsWith("researchtree/categories/")) {
                        LOGGER.warn("Skipping unexpected category path: {}", path);
                        return;
                    }

                    String relativePath = path.substring("researchtree/categories/".length(), path.length() - ".json".length());

                    // Create ID from namespace and filename
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, relativePath.replace('/', '_'));

                    try (Reader reader = resource.openAsReader()) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        jsons.put(id, json);
                        LOGGER.debug("Discovered research category {} from path {}", id, relativePath);
                    } catch (Exception e) {
                        failed.getAndIncrement();
                        LOGGER.error("Failed to read research category {} from {}", id, fileId, e);
                    }
                });

        LOGGER.info("Discovered {} research category JSON(s), with {} failed", jsons.size(), failed.get());
        return jsons;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ResearchCategory> loaded = new HashMap<>();

        for (var entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue();

            ResearchCategory.CODEC.decode(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> LOGGER.error("Error decoding research category {}: {}", id, err))
                    .ifPresent(pair -> {
                        ResearchCategory category = pair.getFirst();
                        // Override the category's ID with the one from the file path
                        ResearchCategory correctedCategory = new ResearchCategory(
                                id,
                                category.name(),
                                category.description(),
                                category.icon(),
                                category.unlockRequirements(),
                                category.sortOrder()
                        );
                        loaded.put(id, correctedCategory);
                        LOGGER.debug("Loaded research category: {}", id);
                    });
        }

        if (ModList.get().isLoaded("kubejs")) {
            KubeJSBridge.applyResearchCategoryModifications(loaded);
        }

        CATEGORIES = Map.copyOf(loaded);
        String label = CATEGORIES.size() == 1 ? "category" : "categories";
        LOGGER.info("Successfully loaded {} research {}", label, CATEGORIES.size());
    }

    public static @UnmodifiableView Map<ResourceLocation, ResearchCategory> getAllCategories() {
        return Collections.unmodifiableMap(CATEGORIES);
    }

    @Nullable
    public static ResearchCategory getCategory(ResourceLocation id) {
        return CATEGORIES.get(id);
    }

    public static @Unmodifiable List<ResearchCategory> getSortedCategories() {
        return CATEGORIES.values().stream()
                .sorted(CATEGORY_ORDER)
                .toList();
    }

    public static @Unmodifiable List<ResearchCategory> getUnlockedCategories(ResearchCategory.PlayerResearchDataAccessor playerData) {
        return CATEGORIES.values().stream()
                .filter(cat -> !cat.isLocked(playerData))
                .sorted(CATEGORY_ORDER)
                .toList();
    }



    public static boolean hasCategory(ResourceLocation id) {
        return CATEGORIES.containsKey(id);
    }
}