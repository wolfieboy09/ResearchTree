package dev.wolfieboy09.researchtree.client.screen.layout;

import com.mojang.logging.LogUtils;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creates an automatic "proper tree" layout for a set of research nodes in a category, based on the prerequisite without
 * requiring manual placement on a grid.
 * <p>
 *     This is a simplified version of Sugiyama-style layered graph drawing pipeline:
 * <ol>
 *     <li>
 *         <b>Layering</b> - Each node is assigned a tier equal to the longest prerequisite chain leading to the node.
 *         Root nodes with no prerequisites are set to tier 0. Every edge therefore only ever points from an earlier tier to a later one.
 *     </li>
 *     <li>
 *         <b>Ordering</b> - Within each tier, nodes are reordered over several alternating up/down sweeps using the barycenter heuristic
 *         (each node is pulled toward the average position of it's placed neighbors in the adjacent tier) to reduce line crossings.
 *     </li>
 *     <li>
 *         <b>Coordinate assignment</b> - Each node's position along the "sibling" axis is repeatedly relaxed toward the average
 *         position of <b>all</b> of its neighbors (parents and children, regardless of how many tiers they span) to
 *         straighten connector lines, while a minimum spacing pass keeps siblings from overlapping.
 *     </li>
 * </ol>
 * The result is expressed as fractional grid unit coordinates ({@link TreeNodePosition}). The caller decides how
 * those map onto pixels and whether the tier axis runs top-to-bottom.
 * @apiNote Edges that skip more than one tier (a node's prerequisite living more than one tier back) are not routed
 * through synthetic "dummy" nodes the way a full Sugiyama implementation would. This keeps the algorithm simple and
 * fast, which is more than enough for research trees with just over a hundred or so nodes. It can occasionally
 * leave a long-skip connector slightly less straight than a full implementation would, but it never affects correctness
 * (nodes never overlap and every prerequisite still points strictly "backwards" along the tier axis)
 */
public final class ResearchTreeLayoutEngine {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Grid units between one tier and the next. Leaves room for the connector lines between nodes. */
    private static final double LAYER_SPACING = 1.5;
    /** Minimum grid units between two sibling nodes on the same tier. */
    private static final double NODE_SPACING = 1.75;
    /** Passes of the barycenter crossing-reduction heuristic. */
    private static final int ORDERING_PASSES = 8;
    /** Passes of the neighbor-averaging coordinate relaxation. */
    private static final int COORDINATE_PASSES = 10;

    private ResearchTreeLayoutEngine() {
    }

    /**
     * Computes a layout for the given nodes (which should all belong to a single category/bucket).
     * Nodes referencing prerequisites outside of this collection simply won't have those edges considered.
     */
    public static Map<ResourceLocation, TreeNodePosition> layout(Collection<ResearchNode> nodes) {
        if (nodes.isEmpty()) {
            return Map.of();
        }

        Map<ResourceLocation, ResearchNode> byId = new HashMap<>();
        for (ResearchNode node : nodes) {
            byId.put(node.id(), node);
        }

        Map<ResourceLocation, List<ResourceLocation>> parents = new HashMap<>();
        Map<ResourceLocation, List<ResourceLocation>> children = new HashMap<>();

        for (ResearchNode node : nodes) {
            List<ResourceLocation> filteredParents = node.prerequisites().stream()
                    .filter(byId::containsKey)
                    .toList();

            parents.put(node.id(), filteredParents);
            children.putIfAbsent(node.id(), new ArrayList<>());

            for (ResourceLocation parent : filteredParents) {
                children.computeIfAbsent(parent, key -> new ArrayList<>()).add(node.id());
            }
        }

        Map<ResourceLocation, Integer> layerOf = computeLayers(byId.keySet(), parents, children);

        int maxLayer = layerOf.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<List<ResourceLocation>> layers = new ArrayList<>();
        for (int i = 0; i <= maxLayer; i++) {
            layers.add(new ArrayList<>());
        }
        for (ResourceLocation id : byId.keySet()) {
            layers.get(layerOf.get(id)).add(id);
        }
        for (List<ResourceLocation> layer : layers) {
            layer.sort(Comparator.comparing(ResourceLocation::toString));
        }

        reduceCrossings(layers, parents, children);
        Map<ResourceLocation, Double> secondaryAxis = assignCoordinates(layers, parents, children);

        Map<ResourceLocation, TreeNodePosition> result = new HashMap<>();
        for (ResourceLocation id : byId.keySet()) {
            double primary = layerOf.get(id) * LAYER_SPACING;
            double secondary = secondaryAxis.get(id);

            result.put(id, new TreeNodePosition(primary, secondary));

//            result.put(id, direction.isHorizontal()
//                    ? new TreeNodePosition(primary, secondary)
//                    : new TreeNodePosition(secondary, primary));
        }

        return result;
    }

    /**
     * Longest-path layering via Kahn's algorithm. Every node's tier is one more than the deepest of its
     * prerequisites, guaranteeing every edge points from an earlier tier to a later one.
     */
    private static Map<ResourceLocation, Integer> computeLayers(
            Set<ResourceLocation> ids,
            Map<ResourceLocation, List<ResourceLocation>> parents,
            Map<ResourceLocation, List<ResourceLocation>> children
    ) {
        Map<ResourceLocation, Integer> indegree = new HashMap<>();
        Map<ResourceLocation, Integer> layer = new HashMap<>();

        for (ResourceLocation id : ids) {
            indegree.put(id, parents.getOrDefault(id, List.of()).size());
        }

        Deque<ResourceLocation> queue = new ArrayDeque<>();
        for (ResourceLocation id : ids) {
            if (indegree.get(id) == 0) {
                queue.add(id);
                layer.put(id, 0);
            }
        }

        int processed = 0;
        while (!queue.isEmpty()) {
            ResourceLocation current = queue.poll();
            processed++;
            int currentLayer = layer.get(current);

            for (ResourceLocation child : children.getOrDefault(current, List.of())) {
                layer.merge(child, currentLayer + 1, Math::max);
                if (indegree.merge(child, -1, Integer::sum) == 0) {
                    queue.add(child);
                }
            }
        }

        if (processed < ids.size()) {
            LOGGER.warn(
                    "Research tree auto layout found a cycle (or self-reference) in prerequisites; {} node(s) could not be layered normally and were pinned to tier 0.",
                    ids.size() - processed
            );
            for (ResourceLocation id : ids) {
                layer.putIfAbsent(id, 0);
            }
        }

        return layer;
    }

    /** Barycenter heuristic crossing reduction, alternating sweeps referencing the previous/next tier. */
    private static void reduceCrossings(
            List<List<ResourceLocation>> layers,
            Map<ResourceLocation, List<ResourceLocation>> parents,
            Map<ResourceLocation, List<ResourceLocation>> children
    ) {
        if (layers.size() < 2) {
            return;
        }

        for (int pass = 0; pass < ORDERING_PASSES; pass++) {
            if (pass % 2 == 0) {
                for (int i = 1; i < layers.size(); i++) {
                    sortByBarycenter(layers.get(i), layers.get(i - 1), parents);
                }
            } else {
                for (int i = layers.size() - 2; i >= 0; i--) {
                    sortByBarycenter(layers.get(i), layers.get(i + 1), children);
                }
            }
        }
    }

    private static void sortByBarycenter(
            List<ResourceLocation> layer,
            List<ResourceLocation> referenceLayer,
            Map<ResourceLocation, List<ResourceLocation>> edgesTowardsReference
    ) {
        Map<ResourceLocation, Integer> referenceIndex = new HashMap<>();
        for (int i = 0; i < referenceLayer.size(); i++) {
            referenceIndex.put(referenceLayer.get(i), i);
        }

        Map<ResourceLocation, Integer> currentIndex = new HashMap<>();
        for (int i = 0; i < layer.size(); i++) {
            currentIndex.put(layer.get(i), i);
        }

        Map<ResourceLocation, Double> barycenter = new HashMap<>();
        for (ResourceLocation id : layer) {
            List<ResourceLocation> neighborsInReference = edgesTowardsReference.getOrDefault(id, List.of()).stream()
                    .filter(referenceIndex::containsKey)
                    .toList();

            if (neighborsInReference.isEmpty()) {
                // No adjacent-tier neighbor to reference (e.g. it's a root, or its only edges skip a tier);
                // keep it where it currently sits rather than yanking it to one side.
                barycenter.put(id, (double) currentIndex.get(id));
            } else {
                double avg = neighborsInReference.stream().mapToInt(referenceIndex::get).average().orElseThrow();
                barycenter.put(id, avg);
            }
        }

        layer.sort(Comparator.comparingDouble(barycenter::get));
    }

    /**
     * Relaxes each node toward the average sibling-axis position of all of its neighbors (parents and children,
     * regardless of tier distance), then enforces a minimum gap between siblings so nothing overlaps.
     */
    private static Map<ResourceLocation, Double> assignCoordinates(
            List<List<ResourceLocation>> layers,
            Map<ResourceLocation, List<ResourceLocation>> parents,
            Map<ResourceLocation, List<ResourceLocation>> children
    ) {
        Map<ResourceLocation, Double> position = new HashMap<>();
        for (List<ResourceLocation> layer : layers) {
            for (int i = 0; i < layer.size(); i++) {
                position.put(layer.get(i), i * NODE_SPACING);
            }
        }

        for (int pass = 0; pass < COORDINATE_PASSES; pass++) {
            boolean forward = pass % 2 == 0;

            for (int i = 0; i < layers.size(); i++) {
                List<ResourceLocation> layer = layers.get(forward ? i : layers.size() - 1 - i);

                for (ResourceLocation id : layer) {
                    List<Double> neighborPositions = new ArrayList<>();
                    for (ResourceLocation parent : parents.getOrDefault(id, List.of())) {
                        neighborPositions.add(position.get(parent));
                    }
                    for (ResourceLocation child : children.getOrDefault(id, List.of())) {
                        neighborPositions.add(position.get(child));
                    }

                    if (!neighborPositions.isEmpty()) {
                        double average = neighborPositions.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
                        position.put(id, average);
                    }
                }

                enforceMinimumSpacing(layer, position);
            }
        }

        double min = position.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
        if (min != 0.0) {
            for (Map.Entry<ResourceLocation, Double> entry : position.entrySet()) {
                entry.setValue(entry.getValue() - min);
            }
        }

        return position;
    }

    /** Keeps siblings in their decided visual order (from {@link #reduceCrossings}) with a minimum gap. */
    private static void enforceMinimumSpacing(List<ResourceLocation> layer, Map<ResourceLocation, Double> position) {
        for (int i = 1; i < layer.size(); i++) {
            double minAllowed = position.get(layer.get(i - 1)) + NODE_SPACING;
            if (position.get(layer.get(i)) < minAllowed) {
                position.put(layer.get(i), minAllowed);
            }
        }
    }
}
