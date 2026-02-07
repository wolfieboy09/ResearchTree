package dev.wolfieboy09.researchtree.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.integration.kubejs.KubeJSBridge;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ResearchNodeManager extends SimplePreparableReloadListener<Map<ResourceLocation, JsonObject>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Map<ResourceLocation, ResearchNode> NODES = Map.of();

    @Override
    protected Map<ResourceLocation, JsonObject> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, JsonObject> jsons = new HashMap<>();
        AtomicInteger failed = new AtomicInteger();

        resourceManager.listResources("researchtree/research", path -> path.getPath().endsWith(".json"))
                .forEach((fileId, resource) -> {
                    String namespace = fileId.getNamespace();
                    String path = fileId.getPath();

                    if (!path.startsWith("researchtree/research/")) {
                        LOGGER.warn("Skipping unexpected research path: {}", path);
                        return;
                    }

                    String relativePath = path.substring("researchtree/research/".length(), path.length() - ".json".length());

                    String fileName = relativePath.contains("/")
                            ? relativePath.substring(relativePath.lastIndexOf('/') + 1)
                            : relativePath;
                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, fileName);

                    String categoryName = relativePath.contains("/")
                            ? relativePath.substring(0, relativePath.lastIndexOf('/'))
                            : "";

                    try (Reader reader = resource.openAsReader()) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                        if (!json.has("category") && !categoryName.isEmpty()) {
                            JsonObject categoryJson = new JsonObject();
                            categoryJson.addProperty("name", categoryName.replace('/', '.'));
                            json.add("category", categoryJson);
                        }

                        jsons.put(id, json);
                        LOGGER.debug("Discovered research node {} from path {}", id, relativePath);
                    } catch (Exception e) {
                        failed.getAndIncrement();
                        LOGGER.error("Failed to read research node {} from {}", id, fileId, e);
                    }
                });

        LOGGER.info("Discovered {} research node JSON(s), with {} failed", jsons.size(), failed.get());
        return jsons;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, ResearchNode> loaded = new HashMap<>();

        for (var entry : jsons.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject json = entry.getValue();

            if (json.has("category") && json.get("category").isJsonPrimitive()) {
                String categoryId = json.get("category").getAsString();

                ResourceLocation catId = ResourceLocation.parse(categoryId);
                ResearchCategory category = ResearchCategoryManager.getCategory(catId);

                if (category != null) {
                    JsonObject categoryJson = new JsonObject();
                    categoryJson.addProperty("name", category.name().getString());
                    json.add("category", ResearchCategory.CODEC.encodeStart(JsonOps.INSTANCE, category).getOrThrow().getAsJsonObject());
                } else {
                    LOGGER.warn("Research node {} references unknown category: {}", id, categoryId);
                }
            }

            ResearchNode.CODEC.decode(JsonOps.INSTANCE, json)
                    .resultOrPartial(err -> LOGGER.error("Error decoding research node {}: {}", id, err))
                    .ifPresent(pair -> {
                        ResearchNode node = pair.getFirst();
                        ResearchNode correctedNode = new ResearchNode(
                                id,
                                node.icon(),
                                node.title(),
                                node.description(),
                                node.prerequisites(),
                                node.requirements(),
                                node.rewards(),
                                node.gridPos(),
                                node.category(),
                                node.hidden(),
                                node.progressData()
                        );
                        loaded.put(id, correctedNode);
                        LOGGER.debug("Loaded research node: {}", id);
                    });
        }

        if (ModList.get().isLoaded("kubejs")) {
            KubeJSBridge.applyResearchNodeModifications(loaded);
        }

        NODES = Map.copyOf(loaded);
        String label = NODES.size() == 1 ? "node" : "nodes";
        LOGGER.info("Successfully loaded {} research {}", NODES.size(), label);
    }

    public static @UnmodifiableView Map<ResourceLocation, ResearchNode> getAllNodes() {
        return Collections.unmodifiableMap(NODES);
    }

    @Nullable
    public static ResearchNode getNode(ResourceLocation id) {
        return NODES.get(id);
    }

    public static Collection<ResearchNode> getRootNodes() {
        return NODES.values().stream()
                .filter(node -> node.prerequisites().isEmpty())
                .toList();
    }

    public static boolean hasNode(ResourceLocation id) {
        return NODES.containsKey(id);
    }
}