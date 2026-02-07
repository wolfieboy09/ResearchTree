package dev.wolfieboy09.researchtree.events;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.network.SyncResearchDataPacket;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@EventBusSubscriber(modid = ResearchTreeMod.MOD_ID)
@ParametersAreNonnullByDefault
public class ResearchEventHandler {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerResearchData data = serverPlayer.getData(RTAttachments.RESEARCH_DATA);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerResearchData data = serverPlayer.getData(RTAttachments.RESEARCH_DATA);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerResearchData data = serverPlayer.getData(RTAttachments.RESEARCH_DATA);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
        }
    }
}