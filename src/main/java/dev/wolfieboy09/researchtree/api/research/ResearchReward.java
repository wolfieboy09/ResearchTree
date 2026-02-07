package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ResearchReward {
    void grant(Player player, Level level);

    Component getDisplayText();

    ResearchRewardType<?> getType();

    Codec<ResearchReward> DISPATCH_CODEC = Codec.lazyInitialized(() -> RTRewardTypes.REWARD_TYPE_REGISTRY.byNameCodec()
            .dispatch(ResearchReward::getType, ResearchRewardType::codec));
}