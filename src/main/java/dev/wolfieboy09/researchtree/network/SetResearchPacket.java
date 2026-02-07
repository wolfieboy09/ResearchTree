package dev.wolfieboy09.researchtree.network;

import dev.wolfieboy09.researchtree.content.blockentity.ResearchTableBlockEntity;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static dev.wolfieboy09.researchtree.ResearchTreeMod.byId;

public record SetResearchPacket(ResourceLocation researchId) implements CustomPacketPayload {

    public static final Type<SetResearchPacket> TYPE = new Type<>(byId("set_research"));

    public static final StreamCodec<ByteBuf, SetResearchPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SetResearchPacket::researchId,
            SetResearchPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(SetResearchPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlayerResearchData data = serverPlayer.getData(RTAttachments.RESEARCH_DATA);

                if (!data.hasResearchTable()) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.researchtree.no_table"));
                    return;
                }

                GlobalPos tablePos = data.getResearchTablePos();
                if (tablePos == null) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.researchtree.no_table"));
                    return;
                }

                ServerLevel level = serverPlayer.server.getLevel(tablePos.dimension());
                if (level == null) {
                    data.clearResearchTablePos();
                    serverPlayer.sendSystemMessage(Component.translatable("message.researchtree.table_missing"));
                    // Sync the cleared table position
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
                    return;
                }

                var blockEntity = level.getBlockEntity(tablePos.pos());
                if (!(blockEntity instanceof ResearchTableBlockEntity table)) {
                    data.clearResearchTablePos();
                    serverPlayer.sendSystemMessage(Component.translatable("message.researchtree.table_missing"));
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
                    return;
                }

                if (table.getOwner() == null || !table.getOwner().equals(serverPlayer.getUUID())) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.researchtree.not_your_table"));
                    return;
                }

                table.setCurrentResearch(packet.researchId);

                PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
            }
        });
    }
}