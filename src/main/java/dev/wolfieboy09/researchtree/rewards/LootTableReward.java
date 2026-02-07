package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LootTableReward(ResourceKey<LootTable> lootTable) implements ResearchReward {
    public static final MapCodec<LootTableReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceKey.codec(Registries.LOOT_TABLE)
                            .fieldOf("loot_table").forGetter(LootTableReward::lootTable)
            ).apply(instance, LootTableReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer) {
            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withParameter(LootContextParams.ORIGIN, player.position())
                    .create(LootContextParamSets.GIFT);

            LootTable table = serverLevel.getServer().reloadableRegistries()
                    .getLootTable(lootTable);

            List<ItemStack> items = table.getRandomItems(params);

            for (ItemStack stack : items) {
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
            }
        }
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(
                "reward.researchtree.loot_table",
                lootTable.location().getPath()
        );
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.LOOT_TABLE.get();
    }
}