package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public record RecipeUnlockReward(List<ResourceLocation> recipes) implements ResearchReward {
    public static final MapCodec<RecipeUnlockReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf()
                            .fieldOf("recipes").forGetter(RecipeUnlockReward::recipes)
            ).apply(instance, RecipeUnlockReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<RecipeHolder<?>> toUnlock = new ArrayList<>();

            for (ResourceLocation recipeId : recipes) {
                serverPlayer.server.getRecipeManager()
                        .byKey(recipeId)
                        .ifPresent(toUnlock::add);
            }

            if (!toUnlock.isEmpty()) {
                serverPlayer.awardRecipes(toUnlock);
            }
        }
    }

    @Override
    public Component getDisplayText() {
        if (recipes.size() == 1) {
            return Component.translatable(
                    "reward.researchtree.recipe.single",
                    recipes.getFirst().getPath()
            );
        }
        return Component.translatable(
                "reward.researchtree.recipe.multiple",
                recipes.size()
        );
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.RECIPE_UNLOCK.get();
    }
}