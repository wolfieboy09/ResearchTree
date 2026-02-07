package dev.wolfieboy09.researchtree.integration.kubejs.wrappers;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiConsumer;

@HideFromJS
public class KubeReward implements ResearchReward {
    private BiConsumer<ServerLevel, ServerPlayer> granter = null;
    private Component displayTextGetter = null;
    private final ResourceLocation rewardId;

    public KubeReward(ResourceLocation rewardId) {
        this.rewardId = rewardId;
    }

    public void setGranter(BiConsumer<ServerLevel, ServerPlayer> granter) {
        this.granter = granter;
    }

    public void setDisplayTextGetter(Component displayTextGetter) {
        this.displayTextGetter = displayTextGetter;
    }

    @Override
    public void grant(Player player, Level level) {
        if (this.granter != null && level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            this.granter.accept(serverLevel, serverPlayer);
        }
    }

    @Override
    public Component getDisplayText() {
        return Objects.requireNonNullElseGet(this.displayTextGetter, () -> Component.translatable("reward.kubejs." + rewardId.getPath()));
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return Objects.requireNonNull(RTRewardTypes.REWARD_TYPE_REGISTRY.get(rewardId));
    }
}