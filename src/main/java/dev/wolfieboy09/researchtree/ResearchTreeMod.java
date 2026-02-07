package dev.wolfieboy09.researchtree;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import dev.wolfieboy09.researchtree.client.screen.ResearchTreeScreen;
import dev.wolfieboy09.researchtree.data.ResearchCategoryManager;
import dev.wolfieboy09.researchtree.data.ResearchNodeManager;
import dev.wolfieboy09.researchtree.integration.kubejs.KubeEventListeners;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.requirements.RTButKubeRequirementRegistry;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.rewards.RTButKubeRewardRegistry;
import dev.wolfieboy09.researchtree.network.*;
import dev.wolfieboy09.researchtree.registries.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(ResearchTreeMod.MOD_ID)
@EventBusSubscriber(modid = ResearchTreeMod.MOD_ID)
public class ResearchTreeMod {
    public static final String MOD_ID = "researchtree";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ResearchTreeMod(IEventBus modEventBus, ModContainer modContainer) {
        RTItems.ITEMS.register(modEventBus);
        RTBlocks.BLOCKS.register(modEventBus);
        RTBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        RTAttachments.ATTACHMENTS.register(modEventBus);
        RTRequirementTypes.REQUIREMENT_TYPES.register(modEventBus);
        RTRewardTypes.REWARD_TYPES.register(modEventBus);
        RTCreativeMenu.REGISTER.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (LoadingModList.get().getModFileById("kubejs") != null) {
            NeoForge.EVENT_BUS.addListener(KubeEventListeners::researchStarted);
            NeoForge.EVENT_BUS.addListener(KubeEventListeners::researchCompleted);
            NeoForge.EVENT_BUS.addListener(KubeEventListeners::requirementCompleted);
            NeoForge.EVENT_BUS.addListener(KubeEventListeners::categoryUnlocked);
            RTButKubeRequirementRegistry.KUBE_REQUIREMENT_TYPES.register(modEventBus);
            RTButKubeRewardRegistry.KUBE_REWARD_TYPES.register(modEventBus);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ResearchCategoryManager());
        event.addListener(new ResearchNodeManager());
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ResearchCompletedPacket.TYPE,
                ResearchCompletedPacket.STREAM_CODEC,
                ResearchCompletedPacket::handleClient
        );
        registrar.playToClient(
                SyncResearchDataPacket.TYPE,
                SyncResearchDataPacket.STREAM_CODEC,
                SyncResearchDataPacket::handleClient
        );
        registrar.playToServer(
                SetResearchPacket.TYPE,
                SetResearchPacket.STREAM_CODEC,
                SetResearchPacket::handleServer
        );
        registrar.playToClient(
                UpdateResearchProgress.TYPE,
                UpdateResearchProgress.STREAM_CODEC,
                UpdateResearchProgress::handleClient
        );

        registrar.playToClient(
                ResearchStartedPacket.TYPE,
                ResearchStartedPacket.STREAM_CODEC,
                ResearchStartedPacket::handleClient
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY);
        event.register(RTRewardTypes.REWARD_TYPE_REGISTRY);
    }

    public static final KeyMapping openTreeScreen = new KeyMapping(
            "key.researchtree.open_research_tree",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "key.categories.researchtree"
    );

    @SubscribeEvent
    public static void registerKeyMapping(RegisterKeyMappingsEvent event) {
        event.register(openTreeScreen);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (openTreeScreen.consumeClick()) {
            Minecraft.getInstance().setScreen(new ResearchTreeScreen());
        }
    }

    public static ResourceLocation byId(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}