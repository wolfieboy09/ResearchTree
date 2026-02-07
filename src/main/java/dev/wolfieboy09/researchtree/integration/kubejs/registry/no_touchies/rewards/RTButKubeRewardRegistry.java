package dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.rewards;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class RTButKubeRewardRegistry {
    public static final DeferredRegister<ResearchRewardType<?>> KUBE_REWARD_TYPES =
            DeferredRegister.create(RTRewardTypes.REWARD_TYPE_REGISTRY_KEY, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<PlayerUnlockStageReward>> POTION_EFFECT =
            create("effect", PlayerUnlockStageReward.CODEC);

    private static <T extends ResearchReward> @NotNull DeferredHolder<ResearchRewardType<?>, ResearchRewardType<T>> create(String id, MapCodec<T> codec) {
        return KUBE_REWARD_TYPES.register(id, () -> new ResearchRewardType<>(codec));
    }
}
