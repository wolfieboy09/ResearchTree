package dev.wolfieboy09.researchtree.client.screen.layout;

/**
 * A computed position for a research node, expressed in fractional grid units
 * (the same unit {@code GridPosition} uses, just not rounded to an integer).
 * <p>
 * {@code x} is the "tier" axis for {@link dev.wolfieboy09.researchtree.api.research.TreeLayoutDirection#LEFT_RIGHT}
 * layouts and the "lane" axis for {@link dev.wolfieboy09.researchtree.api.research.TreeLayoutDirection#TOP_DOWN} layouts (and vice versa for y).
 */
public record TreeNodePosition(double x, double y) {
}
