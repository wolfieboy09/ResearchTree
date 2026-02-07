package dev.wolfieboy09.researchtree.integration.kubejs.wrappers;

import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

@HideFromJS
public class KubeRequirement implements ResearchRequirement<Object> {
    private Predicate<ServerPlayer> checker = null;
    private Function<ServerPlayer, Float> progressGetter = null;
    private Component displayTextGetter = null;
    private final ResourceLocation requirementId;

    public KubeRequirement(ResourceLocation requirementId) {
        this.requirementId = requirementId;
    }

    public void setChecker(Predicate<ServerPlayer> checker) {
        this.checker = checker;
    }

    public void setProgressGetter(Function<ServerPlayer, Float> progressGetter) {
        this.progressGetter = progressGetter;
    }

    public void setDisplayTextGetter(Component displayTextGetter) {
        this.displayTextGetter = displayTextGetter;
    }

    @Override
    public boolean accepts(Object resource) {
        return false;
    }

    @Override
    public Object consume(Player player, Object resource) {
        return resource;
    }

    @Override
    public boolean isMet(Player player) {
        if (this.checker != null && player instanceof ServerPlayer serverPlayer) {
            return this.checker.test(serverPlayer);
        }
        return false;
    }

    @Override
    public float getProgress(Player player) {
        if (this.progressGetter != null && player instanceof ServerPlayer serverPlayer) {
            return this.progressGetter.apply(serverPlayer);
        }
        return 0.0f;
    }

    @Override
    public Component getDisplayText() {
        return Objects.requireNonNullElseGet(this.displayTextGetter, () -> Component.translatable("requirement.kubejs." + requirementId.getPath()));
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return Objects.requireNonNull(RTRequirementTypes.REQUIREMENT_TYPE_REGISTRY.get(requirementId));
    }
}