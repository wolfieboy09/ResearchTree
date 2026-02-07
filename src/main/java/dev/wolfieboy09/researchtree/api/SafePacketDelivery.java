package dev.wolfieboy09.researchtree.api;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class SafePacketDelivery {
    public static void sendToPlayer(@Nullable ServerPlayer player, CustomPacketPayload payload, CustomPacketPayload... payloads) {
        if (player == null) {
            ResearchTreeMod.LOGGER.warn("Attempted to send {} with an additional {} packets to a null player", payload.type().id(), payloads.length);
            return;
        }
        PacketDistributor.sendToPlayer(player, payload, payloads);
    }
}
