package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record PotionEffectReward(
        MobEffectInstance mobEffectInstance
) implements ResearchReward {
    public static final MapCodec<PotionEffectReward> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MobEffectInstance.CODEC.fieldOf("effect").forGetter(PotionEffectReward::mobEffectInstance)
    ).apply(instance, PotionEffectReward::new));

    public PotionEffectReward(MobEffectInstance mobEffectInstance) {
        // Copy the effect just in case...
        this.mobEffectInstance = new MobEffectInstance(mobEffectInstance);
    }

    @Override
    public void grant(Player player, Level level) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.addEffect(this.mobEffectInstance);
        }
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(this.mobEffectInstance.getDescriptionId());
    }

    @Override
    public ResearchRewardType<?> getType() {
        return RTRewardTypes.POTION_EFFECT.get();
    }
}
