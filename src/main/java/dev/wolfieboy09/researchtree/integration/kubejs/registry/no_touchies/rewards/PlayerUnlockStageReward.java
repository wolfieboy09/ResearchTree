package dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record PlayerUnlockStageReward(
        String unlockedStage
) implements ResearchReward {
    public static final MapCodec<PlayerUnlockStageReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("stage").forGetter(PlayerUnlockStageReward::unlockedStage)
            ).apply(instance, PlayerUnlockStageReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        player.kjs$getStages().add(this.unlockedStage);
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable("reward.kubejs.unlocked_stage", this.unlockedStage);
    }

    @Override
    public ResearchRewardType<?> getType() {
        return null;
    }
}
