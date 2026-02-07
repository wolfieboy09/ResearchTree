package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ExperienceReward(int amount, boolean isLevels) implements ResearchReward {
    public static final MapCodec<ExperienceReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("amount").forGetter(ExperienceReward::amount),
                    Codec.BOOL.optionalFieldOf("is_levels", false).forGetter(ExperienceReward::isLevels)
            ).apply(instance, ExperienceReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        if (!level.isClientSide) {
            if (isLevels) {
                player.giveExperienceLevels(amount);
            } else {
                player.giveExperiencePoints(amount);
            }
        }
    }

    @Override
    public Component getDisplayText() {
        if (isLevels) {
            return Component.translatable("reward.researchtree.experience.levels", amount);
        } else {
            return Component.translatable("reward.researchtree.experience.points", amount);
        }
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.EXPERIENCE.get();
    }
}