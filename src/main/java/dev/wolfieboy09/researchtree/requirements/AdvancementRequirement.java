package dev.wolfieboy09.researchtree.requirements;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AdvancementRequirement(
        List<ResourceLocation> requiredAdvancements
) implements ResearchRequirement<ResourceLocation> {
    public static final MapCodec<AdvancementRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf()
                            .fieldOf("advancements").forGetter(AdvancementRequirement::requiredAdvancements)
            ).apply(instance, AdvancementRequirement::new)
    );

    @Override
    public boolean accepts(ResourceLocation advancement) {
        return false;
    }

    @Override
    public ResourceLocation consume(Player player, ResourceLocation advancement) {
        return advancement;
    }

    @Override
    public boolean isMet(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return false;
        }

        var advancements = serverPlayer.getAdvancements();
        for (ResourceLocation advId : requiredAdvancements) {
            AdvancementHolder holder = serverPlayer.getServer()
                    .getAdvancements()
                    .get(advId);

            if (holder == null || !advancements.getOrStartProgress(holder).isDone()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public float getProgress(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return 0.0f;
        }

        int completed = 0;
        var advancements = serverPlayer.getAdvancements();

        for (ResourceLocation advId : requiredAdvancements) {
            AdvancementHolder holder = serverPlayer.getServer()
                    .getAdvancements()
                    .get(advId);

            if (holder != null && advancements.getOrStartProgress(holder).isDone()) {
                completed++;
            }
        }

        return (float) completed / requiredAdvancements.size();
    }

    @Override
    public @NotNull Component getDisplayText() {
        if (requiredAdvancements.size() == 1) {
            return Component.translatable(
                    "requirement.researchtree.advancement.single",
                    requiredAdvancements.getFirst().getPath()
            );
        }
        return Component.translatable(
                "requirement.researchtree.advancement.multiple",
                requiredAdvancements.size()
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.ADVANCEMENT.get();
    }
}