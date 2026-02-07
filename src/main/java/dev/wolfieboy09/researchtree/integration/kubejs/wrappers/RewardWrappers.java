package dev.wolfieboy09.researchtree.integration.kubejs.wrappers;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.rewards.PlayerUnlockStageReward;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import dev.wolfieboy09.researchtree.rewards.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootTable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RewardWrappers {
    public static ItemReward item(Item item, int amount) {
        return new ItemReward(new ItemStack(item, amount));
    }

    public static ItemReward item(ItemStack stack) {
        return new ItemReward(stack.copy());
    }

    public static CommandReward command(String command) {
        return new CommandReward(command, Component.empty());
    }

    public static CommandReward command(String command, Component displayName) {
        return new CommandReward(command, displayName);
    }

    public static ExperienceReward xp(int amount) {
        return new ExperienceReward(amount, false);
    }

    public static ExperienceReward xpLevels(int amount) {
        return new ExperienceReward(amount, true);
    }

    public static AttributeModifierReward attribute(ResourceKey<Attribute> attribute, String modifierId, double amount, AttributeModifier.Operation operation) {
        Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(attribute)
                .orElseThrow(() -> new KubeRuntimeException("Unknown attribute: " + attribute));

        return new AttributeModifierReward(
                attributeHolder,
                ResourceLocation.fromNamespaceAndPath("kubejs", modifierId),
                amount,
                operation
        );
    }

    public static AttributeModifierReward attribute(ResourceKey<Attribute> attribute, ResourceLocation modifierId, double amount, AttributeModifier.Operation operation) {
        Holder<Attribute> attributeHolder = BuiltInRegistries.ATTRIBUTE.getHolder(attribute)
                .orElseThrow(() -> new KubeRuntimeException("Unknown attribute: " + attribute));

        return new AttributeModifierReward(
                attributeHolder,
                modifierId,
                amount,
                operation
        );
    }

    public static AttributeModifierReward attribute(ResourceKey<Attribute> attribute, ResourceLocation modifierId, double amount) {
        return attribute(attribute, modifierId, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    public static AttributeModifierReward attribute(ResourceKey<Attribute> attribute, String modifierId, double amount) {
        return attribute(attribute, modifierId, amount, AttributeModifier.Operation.ADD_VALUE);
    }

    public static LootTableReward lootTable(ResourceLocation lootTableId) {
        return new LootTableReward(ResourceKey.create(
                Registries.LOOT_TABLE,
                lootTableId
        ));
    }

    public static PotionEffectReward effect(MobEffectInstance effectInstance) {
        return new PotionEffectReward(effectInstance);
    }

    public static RecipeUnlockReward unlockRecipes(ResourceLocation recipe) {
        return new RecipeUnlockReward(List.of(recipe));
    }

    public static RecipeUnlockReward unlockRecipes(List<ResourceLocation> recipes) {
        return new RecipeUnlockReward(List.copyOf(recipes));
    }

    public static PlayerUnlockStageReward grantStage(String stage) {
        return new PlayerUnlockStageReward(stage);
    }

    public static KubeReward custom(ResourceLocation id) {
        ResearchRewardType<?> type = RTRewardTypes.REWARD_TYPE_REGISTRY.get(id);

        if (type == null) {
            throw new KubeRuntimeException("Unknown reward type: " + id);
        }

        return (KubeReward) type.codec().codec().parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
    }
}