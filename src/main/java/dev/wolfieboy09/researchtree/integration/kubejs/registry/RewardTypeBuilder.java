package dev.wolfieboy09.researchtree.integration.kubejs.registry;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.RTUtil;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.integration.kubejs.wrappers.KubeReward;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;

public class RewardTypeBuilder extends BuilderBase<ResearchRewardType<?>> {
    private transient BiConsumer<ServerLevel, ServerPlayer> granter = null;
    private transient Component displayTextGetter = null;

    public RewardTypeBuilder(ResourceLocation id) {
        super(id);
    }

    public RewardTypeBuilder grant(BiConsumer<ServerLevel, ServerPlayer> granter) {
        this.granter = granter;
        return this;
    }

    public RewardTypeBuilder displayText(Component displayTextGetter) {
        this.displayTextGetter = displayTextGetter;
        return this;
    }

    @Override
    @HideFromJS
    public ResearchRewardType<?> createObject() {
        MapCodec<KubeReward> codec = MapCodec.unit(() -> {
            KubeReward reward = new KubeReward(id);
            RTUtil.callNotNull(this.granter, reward::setGranter);
            RTUtil.callNotNull(this.displayTextGetter, reward::setDisplayTextGetter);
            return reward;
        });

        return new ResearchRewardType<>(codec);
    }
}