package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ItemReward(
        ItemStack item
) implements ResearchReward {
    public static final MapCodec<ItemReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("item").forGetter(ItemReward::item)
            ).apply(instance, ItemReward::new)
    );

    public ItemReward(ItemStack item) {
        this.item = item.copy();
    }

    @Override
    public void grant(Player player, Level level) {
        if (!level.isClientSide) {
            if (!player.getInventory().add(item.copy())) {
                // If inventory is full just drop it at their location
                player.drop(item.copy(), false);
            }
        }
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(
                "reward.researchtree.item",
                item.getCount(),
                item.getHoverName()
        );
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.ITEM.get();
    }

    @Override
    public ItemStack item() {
        return item.copy();
    }
}