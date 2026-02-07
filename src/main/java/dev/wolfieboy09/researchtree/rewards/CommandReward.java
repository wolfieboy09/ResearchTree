package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record CommandReward(String command, Component displayName) implements ResearchReward {
    public static final MapCodec<CommandReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.STRING.fieldOf("command").forGetter(CommandReward::command),
                    ComponentSerialization.CODEC.optionalFieldOf("display_name", Component.empty()).forGetter(CommandReward::displayName)
            ).apply(instance, CommandReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            CommandSourceStack source = new CommandSourceStack(
                    player,
                    player.position(),
                    player.getRotationVector(),
                    serverLevel,
                    2,
                    player.getName().getString(),
                    player.getDisplayName(),
                    serverLevel.getServer(),
                    player
            );

            try {
                serverLevel.getServer().getCommands().performPrefixedCommand(source, command);
            } catch (Exception e) {
                ResearchTreeMod.LOGGER.error("Failed granting command reward to {} with command: {}", player.getName(), this.command);
                player.sendSystemMessage(Component.translatable("reward.researchtree.failed").withStyle(ChatFormatting.DARK_RED));
            }
        }
    }

    @Override
    public Component getDisplayText() {
        return displayName.getString().isEmpty()
                ? Component.translatable("reward.researchtree.command")
                : displayName;
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.COMMAND.get();
    }
}