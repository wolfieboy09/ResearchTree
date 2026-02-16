package dev.wolfieboy09.researchtree.integration.kubejs.registry.no_touchies.requirements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record PlayerStageRequirement(String stage) implements ResearchRequirement<String> {
    public static final MapCodec<PlayerStageRequirement> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
                Codec.STRING.fieldOf("stage").forGetter(PlayerStageRequirement::stage)
        ).apply(instance, PlayerStageRequirement::new)
    );

    @Override
    public boolean accepts(String resource) {
        return false;
    }

    @Override
    public String consume(Player player, String resource) {
        return resource;
    }

    @Override
    public boolean isMet(Player player) {
        return player.kjs$getStages().has(this.stage);
    }

    @Override
    public float getProgress(Player player) {
        return player.kjs$getStages().has(this.stage) ? 1 : 0;
    }

    @Override
    public @NotNull Component getDisplayText() {
        return Component.translatable("requirement.kubejs.stage", this.stage);
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTButKubeRequirementRegistry.STAGE_UNLOCKED.get();
    }
}
