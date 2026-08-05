package dev.wolfieboy09.researchtree.client.screen;

import dev.wolfieboy09.researchtree.config.RTClientConfig;
import dev.wolfieboy09.researchtree.api.RTUtil;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.client.screen.layout.ResearchTreeLayoutEngine;
import dev.wolfieboy09.researchtree.client.screen.layout.TreeNodePosition;
import dev.wolfieboy09.researchtree.client.screen.widgets.ResearchDetailsPanel;
import dev.wolfieboy09.researchtree.client.screen.widgets.ResearchNodeButton;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.data.ResearchCategoryManager;
import dev.wolfieboy09.researchtree.data.ResearchNodeManager;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class ResearchTreeScreen extends Screen {
    private PlayerResearchData data;
    private ResourceLocation selectedCategoryId = null;
    private final List<Button> categoryButtons = new ArrayList<>();
    private final List<ResearchNodeButton> nodeButtons = new ArrayList<>();

    private ResearchDetailsPanel detailsPanel = null;

    private static final int CATEGORY_PANEL_WIDTH = 120;
    private static final int CATEGORY_BUTTON_HEIGHT = 24;
    private static final int CATEGORY_BUTTON_SPACING = 4;
    private static final int PADDING = 8;

    private double scrollX = 0;
    private double scrollY = 0;
    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;

    private double zoom = 1.0;
    private static final double MIN_ZOOM = 0.35;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.1;

    private double targetScrollX = 0;
    private double targetScrollY = 0;
    private double targetZoom = 1.0;
    private static final double SMOOTH_SCROLL_FACTOR = 0.35;
    private static final double SMOOTH_SCROLL_EPSILON = 0.05;
    private static final double SMOOTH_ZOOM_EPSILON = 0.002;

    private int categoryScrollOffset = 0;
    private static final int CATEGORY_HEADER_HEIGHT = 30;

    private static final int GRID_SIZE = 64;
    private static final int NODE_SIZE = 32;

    // Cached auto layout results, keyed by category id. Cleared whenever
    // nodes/categories are reloaded so datapack/KubeJS changes are picked back up.
    private final Map<ResourceLocation, Map<ResourceLocation, TreeNodePosition>> layoutCache = new HashMap<>();

    public ResearchTreeScreen() {
        super(Component.translatable("screen.researchtree.title"));
    }

    @Override
    protected void init() {
        super.init();

        if (minecraft == null || minecraft.player == null) return;
        data = minecraft.player.getData(RTAttachments.RESEARCH_DATA);

        layoutCache.clear();
        buildCategoryButtons();

        if (selectedCategoryId != null && ResearchCategoryManager.getCategory(selectedCategoryId) == null) {
            selectedCategoryId = null;
        }

        if (selectedCategoryId == null && !categoryButtons.isEmpty()) {
            selectedCategoryId = getFirstAvailableCategory();
        }

        loadNodesForCategory();

        if (detailsPanel != null) {
            detailsPanel.init(width, height);
        }
    }

    private @Nullable ResourceLocation getFirstAvailableCategory() {
        List<ResearchCategory> categories = ResearchCategoryManager.getUnlockedCategories(data);
        return categories.isEmpty() ? null : categories.getFirst().id();
    }

    private void buildCategoryButtons() {
        categoryButtons.forEach(this::removeWidget);
        categoryButtons.clear();

        List<ResearchCategory> unlockedCategories = ResearchCategoryManager.getUnlockedCategories(data);
        List<ResearchCategory> lockedCategories = ResearchCategoryManager.getAllCategories().values()
                .stream()
                .filter(category -> category.isLocked(data))
                .sorted(Comparator.comparingInt(ResearchCategory::sortOrder)
                        .thenComparing(cat -> cat.name().getString()))
                .toList();

        int totalButtonCount = unlockedCategories.size() + lockedCategories.size();
        int visibleAreaHeight = height - CATEGORY_HEADER_HEIGHT - PADDING;
        int totalContentHeight = totalButtonCount * (CATEGORY_BUTTON_HEIGHT + CATEGORY_BUTTON_SPACING);
        int maxScroll = Math.max(0, totalContentHeight - visibleAreaHeight);
        categoryScrollOffset = Math.clamp(categoryScrollOffset, 0, maxScroll);

        int yPos = PADDING + CATEGORY_HEADER_HEIGHT - categoryScrollOffset;

        for (ResearchCategory category : unlockedCategories) {
            if (yPos + CATEGORY_BUTTON_HEIGHT >= CATEGORY_HEADER_HEIGHT && yPos < height) {
                Button categoryBtn = Button.builder(category.name(), btn -> {
                            selectedCategoryId = category.id();
                            loadNodesForCategory();
                        })
                        .bounds(PADDING, yPos, CATEGORY_PANEL_WIDTH - PADDING * 2, CATEGORY_BUTTON_HEIGHT)
                        .build();

                categoryButtons.add(categoryBtn);
                this.addRenderableWidget(categoryBtn);
            }

            yPos += CATEGORY_BUTTON_HEIGHT + CATEGORY_BUTTON_SPACING;
        }

        for (ResearchCategory category : lockedCategories) {
            if (yPos + CATEGORY_BUTTON_HEIGHT >= CATEGORY_HEADER_HEIGHT && yPos < height) {
                Component lockedName = ((MutableComponent) category.name()).withStyle(ChatFormatting.GRAY);
                Button lockedBtn = Button.builder(lockedName, btn -> {})
                        .bounds(PADDING, yPos, CATEGORY_PANEL_WIDTH - PADDING * 2, CATEGORY_BUTTON_HEIGHT)
                        .build();
                lockedBtn.active = false;

                categoryButtons.add(lockedBtn);
                this.addRenderableWidget(lockedBtn);
            }

            yPos += CATEGORY_BUTTON_HEIGHT + CATEGORY_BUTTON_SPACING;
        }
    }

    private void loadNodesForCategory() {
        nodeButtons.forEach(this::removeWidget);
        nodeButtons.clear();

        if (selectedCategoryId == null) return;

        Collection<ResearchNode> nodes = ResearchNodeManager.getAllNodes().values().stream()
                .filter(node -> node.category().equals(selectedCategoryId)).toList();

        ResearchCategory category = ResearchCategoryManager.getCategory(selectedCategoryId);
        boolean useAutoLayout = category == null || category.autoLayout();

        Map<ResourceLocation, TreeNodePosition> positions = useAutoLayout
                ? layoutCache.computeIfAbsent(selectedCategoryId, id ->
                        ResearchTreeLayoutEngine.layout(nodes)) : Map.of();

        for (ResearchNode node : nodes) {
            if (node.hidden() && !shouldShowHiddenNode(node)) {
                continue;
            }

            double gridX;
            double gridY;

            TreeNodePosition pos = positions.get(node.id());
            gridX = pos != null ? pos.x() : 0;
            gridY = pos != null ? pos.y() : 0;

            ResearchNodeButton button = getNodeButton(node, gridX, gridY);

            nodeButtons.add(button);
            this.addRenderableWidget(button);
        }
    }

    private @NotNull ResearchNodeButton getNodeButton(ResearchNode node, double gridX, double gridY) {
        double effectiveGridSize = GRID_SIZE * zoom;
        int nodeSize = Math.max(6, (int) Math.round(NODE_SIZE * zoom));

        int x = CATEGORY_PANEL_WIDTH + PADDING + (int) Math.round(gridX * effectiveGridSize) + (int) scrollX;
        int y = PADDING + 30 + (int) Math.round(gridY * effectiveGridSize) + (int) scrollY;

        return new ResearchNodeButton(
                x, y, nodeSize, nodeSize,
                node.title(),
                btn -> openDetailsPanel(node),
                node,
                data
        );
    }

    private boolean shouldShowHiddenNode(ResearchNode node) {
        return node.prerequisites().stream().anyMatch(data::isCompleted);
    }

    /**
     * Eases scrollX/scrollY/zoom towards their target values when smooth scrolling is enabled,
     * or snaps straight to the target otherwise. Called once per rendered frame.
     */
    private void updateSmoothScrolling() {
        boolean smooth = RTClientConfig.smoothScrollingEnabled.get();

        double dX = targetScrollX - scrollX;
        double dY = targetScrollY - scrollY;
        double dZoom = targetZoom - zoom;

        if (dX == 0 && dY == 0 && dZoom == 0) {
            return;
        }

        boolean settled = !smooth
                || (Math.abs(dX) < SMOOTH_SCROLL_EPSILON
                        && Math.abs(dY) < SMOOTH_SCROLL_EPSILON
                        && Math.abs(dZoom) < SMOOTH_ZOOM_EPSILON);

        if (settled) {
            scrollX = targetScrollX;
            scrollY = targetScrollY;
            zoom = targetZoom;
        } else {
            scrollX += dX * SMOOTH_SCROLL_FACTOR;
            scrollY += dY * SMOOTH_SCROLL_FACTOR;
            zoom += dZoom * SMOOTH_SCROLL_FACTOR;
        }

        loadNodesForCategory();
    }

    private void openDetailsPanel(ResearchNode node) {
        detailsPanel = new ResearchDetailsPanel(node, data,
                () -> {
                    detailsPanel.onStartResearch();
                    closeDetailsPanel();
                },
                this::closeDetailsPanel
        );
        detailsPanel.init(width, height);
    }

    private void closeDetailsPanel() {
        detailsPanel = null;
    }

    @ApiStatus.Internal
    public void refreshNodesAndCategories() {
        layoutCache.clear();
        buildCategoryButtons();
        loadNodesForCategory();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateSmoothScrolling();

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.fill(0, 0, CATEGORY_PANEL_WIDTH, height, 0xAA000000);
        guiGraphics.drawString(font, this.title, PADDING, PADDING, 0xFFFFFFFF);

        int categoryContentTop = PADDING + CATEGORY_HEADER_HEIGHT;
        guiGraphics.enableScissor(0, categoryContentTop, CATEGORY_PANEL_WIDTH, height);

        for (Button btn : categoryButtons) {
            btn.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.disableScissor();

        List<ResearchCategory> unlockedCategories = ResearchCategoryManager.getUnlockedCategories(data);
        List<ResearchCategory> lockedCategories = ResearchCategoryManager.getAllCategories().values()
                .stream()
                .filter(category -> category.isLocked(data))
                .sorted(Comparator.comparingInt(ResearchCategory::sortOrder)
                        .thenComparing(cat -> cat.name().getString()))
                .toList();

        int totalButtonCount = unlockedCategories.size() + lockedCategories.size();
        int visibleAreaHeight = height - CATEGORY_HEADER_HEIGHT - PADDING;
        int totalContentHeight = totalButtonCount * (CATEGORY_BUTTON_HEIGHT + CATEGORY_BUTTON_SPACING);

        if (totalContentHeight > visibleAreaHeight) {
            int scrollbarHeight = Math.max(20, (visibleAreaHeight * visibleAreaHeight) / totalContentHeight);
            int maxScrollbarY = visibleAreaHeight - scrollbarHeight;
            int scrollbarY = categoryContentTop + (int) ((float) categoryScrollOffset / (totalContentHeight - visibleAreaHeight) * maxScrollbarY);

            guiGraphics.fill(CATEGORY_PANEL_WIDTH - 6, scrollbarY, CATEGORY_PANEL_WIDTH - 4, scrollbarY + scrollbarHeight, 0xFF5555FF);
        }

        int treeX = CATEGORY_PANEL_WIDTH;
        int treeY = 0;
        int treeWidth = width - CATEGORY_PANEL_WIDTH;
        int treeHeight = height;

        guiGraphics.enableScissor(treeX, treeY, treeX + treeWidth, treeY + treeHeight);

        renderNodeConnections(guiGraphics);

        for (ResearchNodeButton btn : nodeButtons) {
            btn.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        guiGraphics.disableScissor();

        if (detailsPanel != null) {
            guiGraphics.fill(0, 0, width, height, 0x40000000);
            detailsPanel.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private void renderNodeConnections(GuiGraphics guiGraphics) {
        for (ResearchNodeButton button : nodeButtons) {
            ResearchNode node = button.getNode();

            for (ResourceLocation prereqId : node.prerequisites()) {
                ResearchNodeButton prereqButton = findButtonForNode(prereqId);
                if (prereqButton != null) {
                    drawConnectionLine(guiGraphics, button, prereqButton);
                }
            }
        }
    }

    private @Nullable ResearchNodeButton findButtonForNode(ResourceLocation nodeId) {
        for (ResearchNodeButton btn : nodeButtons) {
            if (btn.getNode().id().equals(nodeId)) {
                return btn;
            }
        }
        return null;
    }

    private void drawConnectionLine(GuiGraphics guiGraphics, ResearchNodeButton from, ResearchNodeButton to) {
        int fromX = from.getX() + from.getWidth() / 2;
        int fromY = from.getY() + from.getHeight() / 2;
        int toX = to.getX() + to.getWidth() / 2;
        int toY = to.getY() + to.getHeight() / 2;

        int color;
        if (data.isCompleted(to.getNode().id())) {
            color = 0xFF00FF00;
        } else if (data.isCompleted(from.getNode().id())) {
            color = 0xFFFFFF00;
        } else {
            color = 0xFF888888;
        }

        drawLine(guiGraphics, fromX, fromY, toX, toY, color);
    }

    private static void drawLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);

        if (steps == 0) return;

        float xStep = (float) (x2 - x1) / steps;
        float yStep = (float) (y2 - y1) / steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= steps; i++) {
            guiGraphics.fill((int) x, (int) y, (int) x + 2, (int) y + 2, color);
            x += xStep;
            y += yStep;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (detailsPanel != null) {
            if (detailsPanel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }

            if (!detailsPanel.isMouseOver(mouseX, mouseY)) {
                closeDetailsPanel();
            }

            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;

        if (detailsPanel != null) {
            detailsPanel.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (detailsPanel != null) {
            if (detailsPanel.mouseDragged(mouseX, mouseY)) {
                return true;
            }
        }

        if (button == 0 && mouseX > CATEGORY_PANEL_WIDTH &&
                (detailsPanel == null || !detailsPanel.isMouseOver(mouseX, mouseY))) {
            if (!isDragging) {
                isDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }

            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            scrollX += deltaX;
            scrollY += deltaY;

            targetScrollX = scrollX;
            targetScrollY = scrollY;

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            loadNodesForCategory();
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (detailsPanel != null) {
            return detailsPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        if (mouseX < CATEGORY_PANEL_WIDTH) {
            List<ResearchCategory> unlockedCategories = ResearchCategoryManager.getUnlockedCategories(data);
            List<ResearchCategory> lockedCategories = ResearchCategoryManager.getAllCategories().values()
                    .stream()
                    .filter(category -> category.isLocked(data))
                    .sorted(Comparator.comparingInt(ResearchCategory::sortOrder)
                            .thenComparing(cat -> cat.name().getString()))
                    .toList();

            int totalButtonCount = unlockedCategories.size() + lockedCategories.size();
            int visibleAreaHeight = height - CATEGORY_HEADER_HEIGHT - PADDING;
            int totalContentHeight = totalButtonCount * (CATEGORY_BUTTON_HEIGHT + CATEGORY_BUTTON_SPACING);
            int maxScroll = Math.max(0, totalContentHeight - visibleAreaHeight);

            categoryScrollOffset = Math.clamp(categoryScrollOffset - (int) (scrollY * 10), 0, maxScroll);
            buildCategoryButtons();
            return true;
        }

        double oldTargetZoom = targetZoom;
        double newTargetZoom = Math.clamp(targetZoom + scrollY * ZOOM_STEP, MIN_ZOOM, MAX_ZOOM);

        if (newTargetZoom != oldTargetZoom) {
            double ratio = newTargetZoom / oldTargetZoom;
            double baseX = CATEGORY_PANEL_WIDTH + PADDING;
            double baseY = PADDING + 30;

            targetScrollX = mouseX - baseX - (mouseX - baseX - targetScrollX) * ratio;
            targetScrollY = mouseY - baseY - (mouseY - baseY - targetScrollY) * ratio;
            targetZoom = newTargetZoom;

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        RTUtil.callNotNull(detailsPanel, this::closeDetailsPanel);
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}