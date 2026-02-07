package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static dev.wolfieboy09.researchtree.ResearchTreeMod.byId;

public record SyncResearchDataPacket(CompoundTag data) implements CustomPacketPayload {

    public static final Type<SyncResearchDataPacket> TYPE = new Type<>(byId("sync_research_data"));

    public static final StreamCodec<ByteBuf, SyncResearchDataPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            SyncResearchDataPacket::data,
            SyncResearchDataPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SyncResearchDataPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null) {
                PlayerResearchData inboundData = PlayerResearchData.load(packet.data);
                PlayerResearchData currentData = player.getData(RTAttachments.RESEARCH_DATA);
                currentData.copyFrom(inboundData);
            }
        });
    }
}