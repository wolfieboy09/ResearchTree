package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.data.ResearchProgress;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static dev.wolfieboy09.researchtree.ResearchTreeMod.byId;

public record UpdateResearchProgress(ResourceLocation research, float updatedProgress) implements CustomPacketPayload {
    public static final Type<UpdateResearchProgress> TYPE = new Type<>(byId("update_research_progress"));

    public static final StreamCodec<ByteBuf, UpdateResearchProgress> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, UpdateResearchProgress::research,
            ByteBufCodecs.FLOAT, UpdateResearchProgress::updatedProgress,
            UpdateResearchProgress::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(UpdateResearchProgress packet, @NotNull IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null) {
                ResearchProgress data = player.getData(RTAttachments.RESEARCH_DATA).getProgress(packet.research);
                if (data != null) {
                    data.setProgress(packet.updatedProgress);
                }
            }
        });
    }
}
