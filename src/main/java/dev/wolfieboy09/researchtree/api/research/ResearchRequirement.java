package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public interface ResearchRequirement<T> {
    boolean accepts(T resource);

    T consume(Player player, T resource);

    default T simulateConsume(Player player, T resource) {
        return resource;
    }

    boolean isMet(Player player);

    float getProgress(Player player);

    Component getDisplayText();

    @NotNull
    ResearchRequirementType<?> getType();

    Codec<ResearchRequirement<?>> DISPATCH_CODEC = Codec.lazyInitialized(() -> RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY.byNameCodec()
            .dispatch(ResearchRequirement::getType, ResearchRequirementType::codec));
}