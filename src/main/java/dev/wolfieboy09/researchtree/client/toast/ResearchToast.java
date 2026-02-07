package dev.wolfieboy09.researchtree.client.toast;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.SafePacketDelivery;
import dev.wolfieboy09.researchtree.api.research.ResearchNode;
import dev.wolfieboy09.researchtree.network.ResearchCompletedPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ResearchToast implements Toast {
    private final Component title;
    private final Component description;
    private final ItemStack icon;
    private long firstDrawTime;
    private boolean changed;

    public static final ResourceLocation TOAST_TEXTURE = ResearchTreeMod.byId("toast/research");

    public ResearchToast(Component title, Component description, ItemStack icon) {
        this.title = title;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public @NotNull Visibility render(GuiGraphics guiGraphics, ToastComponent toastComponent, long timeSinceLastVisible) {
        if (this.changed) {
            this.firstDrawTime = timeSinceLastVisible;
            this.changed = false;
        }

        guiGraphics.blitSprite(TOAST_TEXTURE, 0, 0, this.width(), this.height());

        guiGraphics.drawString(toastComponent.getMinecraft().font, title, 30, 7, 0xFF500050, false);
        guiGraphics.drawString(toastComponent.getMinecraft().font, description, 30, 18, 0xFF000000, false);

        guiGraphics.renderFakeItem(icon, 8, 8);

        return timeSinceLastVisible - this.firstDrawTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }

    public static void addOrUpdate(ServerPlayer player, ResearchNode node) {
        ItemStack icon = node.icon();

        Component title = Component.translatable("toast.researchtree.research_completed");
        Component description = node.title();

        SafePacketDelivery.sendToPlayer(player, new ResearchCompletedPacket(node.id(), title, description, icon));
    }
}