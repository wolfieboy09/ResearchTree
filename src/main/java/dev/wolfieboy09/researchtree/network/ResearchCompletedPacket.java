package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.client.screen.ResearchTreeScreen;
import dev.wolfieboy09.researchtree.client.toast.ResearchToast;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static dev.wolfieboy09.researchtree.ResearchTreeMod.byId;

public record ResearchCompletedPacket(
        ResourceLocation researchId,
        Component title,
        Component description,
        ItemStack icon
) implements CustomPacketPayload {

    public static final Type<ResearchCompletedPacket> TYPE = new Type<>(byId("research_completed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ResearchCompletedPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResearchCompletedPacket::researchId,
            ComponentSerialization.STREAM_CODEC, ResearchCompletedPacket::title,
            ComponentSerialization.STREAM_CODEC, ResearchCompletedPacket::description,
            ItemStack.STREAM_CODEC, ResearchCompletedPacket::icon,
            ResearchCompletedPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchCompletedPacket packet, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.getToasts().addToast(new ResearchToast(packet.title, packet.description, packet.icon));
            LocalPlayer client = mc.player;
            if (client != null) {
                PlayerResearchData data = client.getData(RTAttachments.RESEARCH_DATA);
                data.completeResearch(packet.researchId, null);
                client.setData(RTAttachments.RESEARCH_DATA, data);

                // Trigger refresh in GUI if it's open
                if (mc.screen instanceof ResearchTreeScreen screen) {
                    screen.refreshNodesAndCategories();
                }
            }
        });
    }
}