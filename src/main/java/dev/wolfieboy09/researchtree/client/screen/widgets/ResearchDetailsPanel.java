package dev.wolfieboy09.researchtree.client.screen.widgets;

import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.data.ResearchProgress;
import dev.wolfieboy09.researchtree.network.SetResearchPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class ResearchDetailsPanel {
    private final ResearchNode node;
    private final PlayerResearchData data;

    private int panelWidth;
    private int panelHeight;
    private static final int PADDING = 10;

    private Button startButton;
    private Button closeButton;
    private int scrollOffset = 0;
    private int contentHeight = 0;

    private int panelX;
    private int panelY;

    private boolean isDragging = false;
    private double lastMouseX;
    private double lastMouseY;

    private final Runnable onStart;
    private final Runnable onClose;

    public ResearchDetailsPanel(ResearchNode node, PlayerResearchData data, Runnable onStart, Runnable onClose) {
        this.node = node;
        this.data = data;
        this.onStart = onStart;
        this.onClose = onClose;
    }

    public void init(int screenWidth, int screenHeight) {
        panelWidth = Math.min(280, screenWidth - 40);
        panelHeight = Math.min(380, screenHeight - 40);

        panelX = screenWidth - panelWidth - 20;
        panelY = (screenHeight - panelHeight) / 2;

        initButtons();
    }


    private void initButtons() {
        boolean canStart = !data.isCompleted(node.id()) && canStartResearch();
        boolean isCompleted = data.isCompleted(node.id());
        boolean isInProgress = data.getProgress(node.id()) != null;

        int buttonY = panelY + panelHeight - 30;

        if (isCompleted || isInProgress) {
            closeButton = Button.builder(
                    Component.translatable("gui.researchtree.close"),
                    btn -> onClose.run()
            ).bounds(panelX + panelWidth / 2 - 60, buttonY, 120, 20).build();
            startButton = null;
        } else {
            startButton = Button.builder(
                    Component.translatable("gui.researchtree.start_research"),
                    btn -> onStart.run()
            ).bounds(panelX + PADDING, buttonY, 120, 20).build();
            startButton.active = canStart;

            // Close button
            closeButton = Button.builder(
                    Component.literal("×"),
                    btn -> onClose.run()
            ).bounds(panelX + panelWidth - PADDING - 20, buttonY, 20, 20).build();
        }
    }

    private boolean canStartResearch() {
        if (!data.hasResearchTable()) {
            return false;
        }

        for (ResourceLocation prereq : node.prerequisites()) {
            if (!data.isCompleted(prereq)) {
                return false;
            }
        }
        return true;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 20);
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0101010);

        // Border
        guiGraphics.fill(panelX - 2, panelY, panelX, panelY + panelHeight, 0xFF5555FF); // Left border
        guiGraphics.fill(panelX, panelY - 2, panelX + panelWidth, panelY, 0xFF5555FF); // Top border
        guiGraphics.fill(panelX + panelWidth, panelY, panelX + panelWidth + 2, panelY + panelHeight, 0xFF5555FF); // Right border
        guiGraphics.fill(panelX, panelY + panelHeight, panelX + panelWidth, panelY + panelHeight + 2, 0xFF5555FF); // Bottom border

        Minecraft mc = Minecraft.getInstance();

        int contentTop = panelY + PADDING;
        int contentBottom = panelY + panelHeight - 50; // Leave room for buttons
        guiGraphics.enableScissor(panelX, contentTop, panelX + panelWidth, contentBottom);

        int yPos = contentTop - scrollOffset;

        var stack = node.icon().copy();
        guiGraphics.renderFakeItem(stack, panelX + PADDING, yPos);

        // Title
        List<String> titleLines = wrapText(node.title().getString(), panelWidth - 40, mc.font);
        for (String line : titleLines) {
            guiGraphics.drawString(mc.font, line, panelX + PADDING + 20, yPos + 4, 0xFFFFFFFF);
            yPos += 10;
        }
        yPos += 8;

        // Status
        if (data.isCompleted(node.id())) {
            Component status = Component.translatable("gui.researchtree.status.completed")
                    .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);
            guiGraphics.drawString(mc.font, status, panelX + PADDING, yPos, 0xFF00FF00);
            yPos += 14;
        } else if (data.getProgress(node.id()) != null) {
            ResearchProgress progress = data.getProgress(node.id());
            Component status = Component.translatable("gui.researchtree.status.in_progress",
                            String.format("%.1f%%", progress.getProgress() * 100))
                    .withStyle(ChatFormatting.YELLOW);
            guiGraphics.drawString(mc.font, status, panelX + PADDING, yPos, 0xFFFFFF00);
            yPos += 12;

            // Progress bar
            int barWidth = panelWidth - PADDING * 2;
            guiGraphics.fill(panelX + PADDING, yPos, panelX + PADDING + barWidth, yPos + 6, 0xFF333333);
            int progressWidth = (int)(barWidth * progress.getProgress());
            guiGraphics.fill(panelX + PADDING, yPos, panelX + PADDING + progressWidth, yPos + 6, 0xFF00FF00);
            yPos += 10;
        }

        // Description
        if (!node.description().getString().isEmpty()) {
            List<String> descLines = wrapText(node.description().getString(), panelWidth - PADDING * 2, mc.font);
            for (String line : descLines) {
                guiGraphics.drawString(mc.font, line, panelX + PADDING, yPos, 0xFFAAAAAA);
                yPos += 9;
            }
            yPos += 6;
        }

        // Prerequisites
        if (!node.prerequisites().isEmpty()) {
            guiGraphics.drawString(mc.font,
                    Component.translatable("gui.researchtree.prerequisites").withStyle(ChatFormatting.UNDERLINE),
                    panelX + PADDING, yPos, 0xFFFFFFFF);
            yPos += 11;

            for (ResourceLocation prereq : node.prerequisites()) {
                boolean completed = data.isCompleted(prereq);
                String symbol = completed ? "✓" : "✗";
                Component prereqText = Component.literal(symbol + " " + prereq.getPath());
                guiGraphics.drawString(mc.font, prereqText, panelX + PADDING + 4, yPos,
                        completed ? 0xFF00FF00 : 0xFFFF5555);
                yPos += 9;
            }
            yPos += 6;
        }

        // Requirements
        guiGraphics.drawString(mc.font,
                Component.translatable("gui.researchtree.requirements").withStyle(ChatFormatting.UNDERLINE),
                panelX + PADDING, yPos, 0xFFFFFFFF);
        yPos += 11;

        if (node.requirements().isEmpty()) {
            guiGraphics.drawString(mc.font,
                    Component.translatable("gui.researchtree.no_requirements").withStyle(ChatFormatting.ITALIC),
                    panelX + PADDING + 4, yPos, 0xFF888888);
            yPos += 9;
        } else {
            for (ResearchRequirement<?> req : node.requirements()) {
                Component reqText = Component.literal("• ").append(req.getDisplayText());
                guiGraphics.drawString(mc.font, reqText, panelX + PADDING + 4, yPos, 0xFFCCCCCC);
                yPos += 9;
            }
        }
        yPos += 6;

        // Rewards
        guiGraphics.drawString(mc.font,
                Component.translatable("gui.researchtree.rewards").withStyle(ChatFormatting.UNDERLINE),
                panelX + PADDING, yPos, 0xFFFFFFFF);
        yPos += 11;

        if (node.rewards().isEmpty()) {
            guiGraphics.drawString(mc.font,
                    Component.translatable("gui.researchtree.no_rewards").withStyle(ChatFormatting.ITALIC),
                    panelX + PADDING + 4, yPos, 0xFF888888);
            yPos += 9;
        } else {
            for (ResearchReward reward : node.rewards()) {
                Component rewardText = Component.literal("• ").append(reward.getDisplayText())
                        .withStyle(ChatFormatting.GOLD);
                guiGraphics.drawString(mc.font, rewardText, panelX + PADDING + 4, yPos, 0xFFFFAA00);
                yPos += 9;
            }
        }

        contentHeight = yPos - (contentTop - scrollOffset);

        guiGraphics.disableScissor();

        // Warning message if it can't start
        if (startButton != null && !startButton.active) {
            Component warning;
            if (!data.hasResearchTable()) {
                warning = Component.translatable("gui.researchtree.warning.no_table");
            } else {
                warning = Component.translatable("gui.researchtree.warning.missing_prerequisites");
            }

            List<String> warningLines = wrapText(warning.getString(), panelWidth - PADDING * 2, mc.font);
            int warningY = panelY + panelHeight - 52;
            for (String line : warningLines) {
                int warningX = panelX + panelWidth / 2 - mc.font.width(line) / 2;
                guiGraphics.drawString(mc.font, Component.literal(line).withStyle(ChatFormatting.RED),
                        warningX, warningY, 0xFFFF5555);
                warningY += 9;
            }
        }

        if (startButton != null) {
            startButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        closeButton.render(guiGraphics, mouseX, mouseY, partialTick);

        int maxScroll = Math.max(0, contentHeight - (contentBottom - contentTop));
        if (maxScroll > 0) {
            int scrollbarHeight = 40;
            int scrollbarY = contentTop + (int)((contentBottom - contentTop - scrollbarHeight) *
                    ((float)scrollOffset / maxScroll));
            guiGraphics.fill(panelX + panelWidth - 6, scrollbarY,
                    panelX + panelWidth - 4, scrollbarY + scrollbarHeight, 0xFF5555FF);
        }

        guiGraphics.pose().popPose();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (startButton != null && startButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (closeButton.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        // Check if clicking inside panel for dragging
        if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + 30) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }

        return isMouseOver(mouseX, mouseY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;

        if (startButton != null) {
            startButton.mouseReleased(mouseX, mouseY, button);
        }
        closeButton.mouseReleased(mouseX, mouseY, button);

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
        if (isDragging) {
            panelX += (int)(mouseX - lastMouseX);
            panelY += (int)(mouseY - lastMouseY);

            panelX = Math.max(0, Math.min(Minecraft.getInstance().getWindow().getGuiScaledWidth() - panelWidth, panelX));
            panelY = Math.max(0, Math.min(Minecraft.getInstance().getWindow().getGuiScaledHeight() - panelHeight, panelY));

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            initButtons();
            return true;
        }

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMouseOver(mouseX, mouseY)) {
            int contentAreaHeight = (panelHeight - 50) - PADDING;
            int maxScroll = Math.max(0, contentHeight - contentAreaHeight);
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(scrollY * 10)));
            return true;
        }
        return false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + panelHeight;
    }

    private List<String> wrapText(String text, int maxWidth, net.minecraft.client.gui.Font font) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (font.width(testLine) <= maxWidth) {
                if (!currentLine.isEmpty()) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    lines.add(word);
                }
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public void onStartResearch() {
        if (data.hasResearchTable()) {
            PacketDistributor.sendToServer(new SetResearchPacket(node.id()));
        }
    }

    public Button getStartButton() {
        return startButton;
    }

    public Button getCloseButton() {
        return closeButton;
    }

    public ResearchNode getNode() {
        return node;
    }
}