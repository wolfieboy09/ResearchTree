package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.client.screen.ResearchTreeScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static dev.wolfieboy09.researchtree.ResearchTreeMod.byId;

public record OpenResearchScreenPacket() implements CustomPacketPayload {
    public static final Type<OpenResearchScreenPacket> TYPE = new Type<>(byId("open_research_screen"));

    public static final StreamCodec<ByteBuf, OpenResearchScreenPacket> STREAM_CODEC = StreamCodec.unit(new OpenResearchScreenPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(OpenResearchScreenPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> Minecraft.getInstance().setScreen(new ResearchTreeScreen()));
    }
}
