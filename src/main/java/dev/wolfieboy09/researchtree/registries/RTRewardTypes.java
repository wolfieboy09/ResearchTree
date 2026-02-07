package dev.wolfieboy09.researchtree.registries;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.rewards.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;
import org.jetbrains.annotations.NotNull;

public final class RTRewardTypes {
    public static final ResourceKey<Registry<ResearchRewardType<?>>> REWARD_TYPE_REGISTRY_KEY =
            ResourceKey.createRegistryKey(ResearchTreeMod.byId("reward_types"));

    public static final Registry<ResearchRewardType<?>> REWARD_TYPE_REGISTRY =
            new RegistryBuilder<>(REWARD_TYPE_REGISTRY_KEY)
                    .sync(true)
                    .create();

    public static final DeferredRegister<ResearchRewardType<?>> REWARD_TYPES =
            DeferredRegister.create(REWARD_TYPE_REGISTRY_KEY, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<ItemReward>> ITEM =
            create("item", ItemReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<CommandReward>> COMMAND =
            create("command", CommandReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<ExperienceReward>> EXPERIENCE =
            create("experience", ExperienceReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<PotionEffectReward>> POTION_EFFECT =
            create("effect", PotionEffectReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<AttributeModifierReward>> ATTRIBUTE_MODIFIER =
            create("attribute_modifier", AttributeModifierReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<RecipeUnlockReward>> RECIPE_UNLOCK =
            create("recipe_unlock", RecipeUnlockReward.CODEC);

    public static final DeferredHolder<ResearchRewardType<?>, ResearchRewardType<LootTableReward>> LOOT_TABLE =
            create("loot_table", LootTableReward.CODEC);

    private static <T extends ResearchReward> @NotNull DeferredHolder<ResearchRewardType<?>, ResearchRewardType<T>> create(String id, MapCodec<T> codec) {
        return REWARD_TYPES.register(id, () -> new ResearchRewardType<>(codec));
    }
}