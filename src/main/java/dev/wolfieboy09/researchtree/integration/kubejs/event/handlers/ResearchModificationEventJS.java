package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.data.ResearchNodeManager;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class ResearchModificationEventJS implements KubeEvent {
    private final transient Map<ResourceLocation, ResearchNodeBuilder> nodesToAdd = new HashMap<>();
    private final transient Set<ResourceLocation> nodesToRemove = new HashSet<>();
    private final transient Map<ResourceLocation, Consumer<ResearchNodeBuilder>> nodesToModify = new HashMap<>();

    public void remove(ResourceLocation id) {
        nodesToRemove.add(id);
    }

    public ResearchNodeBuilder modify(ResourceLocation id) {
        ResearchNode existing = ResearchNodeManager.getNode(id);
        if (existing == null) {
            throw new KubeRuntimeException("Research node does not exist: " + id);
        }

        ResearchNodeBuilder builder = new ResearchNodeBuilder(existing);

        nodesToModify.put(id, b -> {});
        return builder;
    }

    public ResearchNodeBuilder create(KubeResourceLocation id) {
        ResearchNodeBuilder builder = new ResearchNodeBuilder(id.wrapped());
        nodesToAdd.put(id.wrapped(), builder);
        builder.setParentEvent(this);
        return builder;
    }

    public boolean exists(ResourceLocation id) {
        return ResearchNodeManager.hasNode(id);
    }

    @HideFromJS
    void registerNode(ResourceLocation id, ResearchNodeBuilder node) {
        nodesToAdd.putIfAbsent(id, node);
    }

    @HideFromJS
    public Map<ResourceLocation, ResearchNodeBuilder> getNodesToAdd() {
        return nodesToAdd;
    }

    @HideFromJS
    public Set<ResourceLocation> getNodesToRemove() {
        return nodesToRemove;
    }

    @HideFromJS
    public Map<ResourceLocation, Consumer<ResearchNodeBuilder>> getNodesToModify() {
        return nodesToModify;
    }

    @HideFromJS
    public void clearCache() {
        nodesToAdd.clear();
        nodesToModify.clear();
        nodesToRemove.clear();
    }
}