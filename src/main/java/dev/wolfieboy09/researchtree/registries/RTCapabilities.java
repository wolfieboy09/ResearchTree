package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.content.blockentity.ResearchTableBlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = ResearchTreeMod.MOD_ID)
public class RTCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                RTBlockEntities.RESEARCH_TABLE.get(),
                ResearchTableBlockEntity::getItemHandler
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                RTBlockEntities.RESEARCH_TABLE.get(),
                ResearchTableBlockEntity::getFluidHandler
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                RTBlockEntities.RESEARCH_TABLE.get(),
                ResearchTableBlockEntity::getEnergyHandler
        );
    }
}