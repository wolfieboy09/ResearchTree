package dev.wolfieboy09.researchtree.core;

import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ResearchTree {
    private final Map<ResourceLocation, ResearchNode> nodes = new HashMap<>();
    private final Map<ResourceLocation, List<ResourceLocation>> childrenMap = new HashMap<>();

    public void addNode(ResearchNode node) {
        nodes.put(node.id(), node);

        for (ResourceLocation prereq : node.prerequisites()) {
            childrenMap.computeIfAbsent(prereq, k -> new ArrayList<>()).add(node.id());
        }
    }

    public ResearchNode getNode(ResourceLocation id) {
        return nodes.get(id);
    }

    public Collection<ResearchNode> getAllNodes() {
        return nodes.values();
    }

    public List<ResourceLocation> getChildren(ResourceLocation nodeId) {
        return childrenMap.getOrDefault(nodeId, List.of());
    }

    public List<ResearchNode> getRootNodes() {
        return nodes.values().stream()
                .filter(node -> node.prerequisites().isEmpty())
                .toList();
    }

    public boolean canUnlock(ResourceLocation nodeId, Set<ResourceLocation> completedResearch) {
        ResearchNode node = nodes.get(nodeId);
        if (node == null) return false;

        for (ResourceLocation prereq : node.prerequisites()) {
            if (!completedResearch.contains(prereq)) {
                return false;
            }
        }
        return true;
    }
}