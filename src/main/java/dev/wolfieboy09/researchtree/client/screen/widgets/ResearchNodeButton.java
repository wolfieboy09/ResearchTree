package dev.wolfieboy09.researchtree.client.screen.widgets;

import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.data.ResearchProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ResearchNodeButton extends Button {
    private final ResearchNode node;
    private final PlayerResearchData data;
    private float hoverScale = 0.0f;
    private float researchAnimationTicks = 0.0f;
    private boolean wasCompleted = false;
    private boolean wasHovered = false;

    private static final float MAX_HOVER_SCALE = 1.15f;
    private static final float SCALE_SPEED = 0.15f;

    public ResearchNodeButton(int x, int y, int width, int height,
                              Component message, OnPress onPress,
                              ResearchNode node, PlayerResearchData data) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.node = node;
        this.data = data;

        this.active = !data.isCompleted(node.id()) && canStart(node, data);

        updateTooltip();
    }

    private void updateTooltip() {
        List<Component> tooltipLines = new ArrayList<>();
        tooltipLines.add(node.title().copy().withStyle(style -> style.withBold(true)));

        if (!node.description().getString().isEmpty()) {
            tooltipLines.add(Component.empty());
            tooltipLines.add(node.description());
        }

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            tooltipLines.add(Component.empty());
            tooltipLines.add(Component.translatable("tooltip.researchtree.research_node_id", node.id().toString()).withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        MutableComponent tooltip = Component.empty();

        for (int i = 0; i < tooltipLines.size(); i++) {
            if (i > 0) tooltip.append(Component.literal("\n"));
            tooltip.append(tooltipLines.get(i));
        }

        this.setTooltip(Tooltip.create(tooltip));
    }

    private boolean canStart(ResearchNode node, PlayerResearchData data) {
        for (ResourceLocation prereq : node.prerequisites()) {
            if (!data.isCompleted(prereq)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update tooltip when we first start hovering
        if (isHovered()) {
            if (!wasHovered) {
                updateTooltip();
            }
            wasHovered = true;
        } else {
            wasHovered = false;
        }

        if (isHovered() && hoverScale < 1.0f) {
            hoverScale = Math.min(1.0f, hoverScale + SCALE_SPEED);
        } else if (!isHovered() && hoverScale > 0.0f) {
            hoverScale = Math.max(0.0f, hoverScale - SCALE_SPEED);
        }

        // Check if research just completed
        boolean isCompleted = data.isCompleted(node.id());
        if (isCompleted && !wasCompleted) {
            updateTooltip();
            wasCompleted = true;
        }

        // Update research animation and tooltip during active research
        ResearchProgress progress = data.getProgress(node.id());
        if (progress != null && !progress.isComplete()) {
            updateTooltip();
            researchAnimationTicks += partialTick;
        }

        // Calculate scale
        float scale = 1.0f + (hoverScale * (MAX_HOVER_SCALE - 1.0f));

        // Calculate scaled dimensions
        int scaledWidth = (int) (width * scale);
        int scaledHeight = (int) (height * scale);
        int offsetX = (scaledWidth - width) / 2;
        int offsetY = (scaledHeight - height) / 2;

        int renderX = getX() - offsetX;
        int renderY = getY() - offsetY;

        guiGraphics.pose().pushPose();

        // Render background
        int bgColor = getBackgroundColor();
        guiGraphics.fill(renderX, renderY, renderX + scaledWidth, renderY + scaledHeight, bgColor);

        // Render research animation (spinning yellow lines)
        if (progress != null && !progress.isComplete()) {
            renderResearchAnimation(guiGraphics, renderX, renderY, scaledWidth, scaledHeight);
        }

        // Render border with progress bar
        int borderColor = getBorderColor();
        renderBorderWithProgress(guiGraphics, renderX, renderY, scaledWidth, scaledHeight, borderColor, progress);

        // Render icon in the center
        ItemStack icon = node.icon();
        int iconX = renderX + (scaledWidth - 16) / 2;
        int iconY = renderY + (scaledHeight - 16) / 2;
        guiGraphics.renderFakeItem(icon, iconX, iconY);

        guiGraphics.pose().popPose();
    }

    private int getBackgroundColor() {
        if (data.isCompleted(node.id())) {
            return 0xFF00AA00; // Green for completed
        } else if (data.getProgress(node.id()) != null) {
            return 0xFFAAAA00; // Yellow for in progress
        } else if (!active) {
            return 0xFF3F3F3F; // Dark gray for locked
        } else {
            return 0xFF555555; // Grey for available
        }
    }

    private int getBorderColor() {
        if (isHovered()) {
            return 0xFFFFFFFF; // White when hovered
        } else if (data.isCompleted(node.id())) {
            return 0xFF00FF00; // Bright green for completed
        } else if (!active) {
            return 0xFF888888; // Light gray for locked
        } else {
            return 0xFFAAAAAA; // Medium gray for available
        }
    }

    private void renderBorderWithProgress(GuiGraphics guiGraphics, int x, int y, int width, int height, int color,@Nullable ResearchProgress progress) {
        int borderThickness = 2;

        if (progress != null && !progress.isComplete()) {
            // Calculate progress bar parameters
            float progressPercent = progress.getProgress();
            int perimeter = (width + height) * 2 - 8; // Total perimeter minus corners
            int progressLength = (int) (perimeter * progressPercent);

            int progressColor = 0xFF00FF00; // Bright green for progress
            int baseColor = 0xFF333333; // Dark gray for base border

            // Render base border first
            renderBorder(guiGraphics, x, y, width, height, baseColor, borderThickness);

            // Render progress overlay
            renderProgressBorder(guiGraphics, x, y, width, height, progressColor, borderThickness, progressLength);
        } else {
            // Normal border
            renderBorder(guiGraphics, x, y, width, height, color, borderThickness);
        }
    }

    private void renderBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int thickness) {
        // Top
        guiGraphics.fill(x, y, x + width, y + thickness, color);
        // Bottom
        guiGraphics.fill(x, y + height - thickness, x + width, y + height, color);
        // Left
        guiGraphics.fill(x, y, x + thickness, y + height, color);
        // Right
        guiGraphics.fill(x + width - thickness, y, x + width, y + height, color);
    }

    private void renderProgressBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color, int thickness, int progressLength) {
        int remaining = progressLength;

        // Top edge (left to right)
        int topLength = Math.min(remaining, width - thickness);
        if (topLength > 0) {
            guiGraphics.fill(x, y, x + topLength, y + thickness, color);
            remaining -= topLength;
        }

        if (remaining <= 0) return;

        // Right edge (top to bottom)
        int rightLength = Math.min(remaining, height - thickness);
        if (rightLength > 0) {
            guiGraphics.fill(x + width - thickness, y, x + width, y + rightLength, color);
            remaining -= rightLength;
        }

        if (remaining <= 0) return;

        // Bottom edge (right to left)
        int bottomLength = Math.min(remaining, width - thickness);
        if (bottomLength > 0) {
            guiGraphics.fill(x + width - bottomLength, y + height - thickness, x + width, y + height, color);
            remaining -= bottomLength;
        }

        if (remaining <= 0) return;

        // Left edge (bottom to top)
        int leftLength = Math.min(remaining, height - thickness);
        if (leftLength > 0) {
            guiGraphics.fill(x, y + height - leftLength, x + thickness, y + height, color);
        }
    }

    private void renderResearchAnimation(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        float animProgress = (researchAnimationTicks % 60.0f) / 60.0f; // 3 second loop (60 ticks)

        // Calculate line positions
        int perimeter = (width + height) * 2;
        int line1Pos = (int) (perimeter * animProgress);
        int line2Pos = (int) (perimeter * ((animProgress + 0.5f) % 1.0f));

        // Render both lines
        renderAnimationLine(guiGraphics, x, y, width, height, line1Pos, perimeter);
        renderAnimationLine(guiGraphics, x, y, width, height, line2Pos, perimeter);
    }

    private void renderAnimationLine(GuiGraphics guiGraphics, int x, int y, int width, int height, int position, int perimeter) {
        int lineLength = 8; // Length of each line segment
        int lineColor = 0xFFFFFF00; // Yellow
        int lineThickness = 3;

        // Top edge
        if (position < width) {
            int startX = x + position;
            int endX = Math.min(x + width, startX + lineLength);
            guiGraphics.fill(startX, y, endX, y + lineThickness, lineColor);
        }
        // Right edge
        else if (position < width + height) {
            int relPos = position - width;
            int startY = y + relPos;
            int endY = Math.min(y + height, startY + lineLength);
            guiGraphics.fill(x + width - lineThickness, startY, x + width, endY, lineColor);
        }
        // Bottom edge
        else if (position < width * 2 + height) {
            int relPos = position - width - height;
            int startX = x + width - relPos;
            int endX = Math.max(x, startX - lineLength);
            guiGraphics.fill(endX, y + height - lineThickness, startX, y + height, lineColor);
        }
        // Left edge
        else {
            int relPos = position - width * 2 - height;
            int startY = y + height - relPos;
            int endY = Math.max(y, startY - lineLength);
            guiGraphics.fill(x, endY, x + lineThickness, startY, lineColor);
        }
    }

    public ResearchNode getNode() {
        return node;
    }
}