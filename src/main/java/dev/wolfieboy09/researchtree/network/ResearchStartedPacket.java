package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ResearchStartedPacket(ResourceLocation researchId) implements CustomPacketPayload {
    public static final Type<ResearchStartedPacket> TYPE = new Type<>(ResearchTreeMod.byId("research_started"));

    public static final StreamCodec<ByteBuf, ResearchStartedPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, ResearchStartedPacket::researchId,
            ResearchStartedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ResearchStartedPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer client = Minecraft.getInstance().player;
            if (client != null) {
                PlayerResearchData data = client.getData(RTAttachments.RESEARCH_DATA);
                data.startResearch(packet.researchId);
                client.setData(RTAttachments.RESEARCH_DATA, data);
            }
        });
    }
}
