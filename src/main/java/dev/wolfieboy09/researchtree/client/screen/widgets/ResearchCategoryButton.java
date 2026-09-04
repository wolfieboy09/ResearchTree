package dev.wolfieboy09.researchtree.client.screen.widgets;

import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import org.jetbrains.annotations.NotNull;

public class ResearchCategoryButton extends Button {
    private final ResearchCategory category;

    public ResearchCategoryButton(int x, int y, int width, int height, ResearchCategory category, OnPress onPress) {
        super(x, y, width, height, category.name(), onPress, DEFAULT_NARRATION);
        this.category = category;
    }

    public ResearchCategoryButton(int x, int y, int width, int height, ResearchCategory category) {
        this(x, y, width, height, category, b -> {});
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, 0xAA727272);
        gui.drawCenteredString(minecraft.font, category.name(), this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 3, 0xFFFFFF);
    }
}
