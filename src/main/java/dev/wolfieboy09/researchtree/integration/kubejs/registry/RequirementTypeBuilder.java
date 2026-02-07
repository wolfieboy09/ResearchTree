package dev.wolfieboy09.researchtree.integration.kubejs.registry;

import com.mojang.serialization.MapCodec;
import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.RTUtil;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.integration.kubejs.wrappers.KubeRequirement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;
import java.util.function.Predicate;

public class RequirementTypeBuilder extends BuilderBase<ResearchRequirementType<?>> {
    private transient Predicate<ServerPlayer> checker = null;
    private transient Function<ServerPlayer, Float> progressGetter = null;
    private transient Component displayTextGetter = null;

    public RequirementTypeBuilder(ResourceLocation id) {
        super(id);
    }

    public RequirementTypeBuilder isMet(Predicate<ServerPlayer> checker) {
        this.checker = checker;
        return this;
    }

    public RequirementTypeBuilder progress(Function<ServerPlayer, Float> progressGetter) {
        this.progressGetter = progressGetter;
        return this;
    }

    public RequirementTypeBuilder displayText(Component displayTextGetter) {
        this.displayTextGetter = displayTextGetter;
        return this;
    }

    @Override
    @HideFromJS
    public ResearchRequirementType<?> createObject() {
        MapCodec<KubeRequirement> codec = MapCodec.unit(() -> {
            KubeRequirement req = new KubeRequirement(id);
            if (this.checker == null) {
                throw new KubeRuntimeException("The isMet method is not defined for id: " + id);
            }

            if (this.progressGetter == null) {
                throw new KubeRuntimeException("The progress method is not defined for id: " + id);
            }

            RTUtil.callNotNull(this.checker, req::setChecker);
            RTUtil.callNotNull(this.progressGetter, req::setProgressGetter);
            RTUtil.callNotNull(this.displayTextGetter, req::setDisplayTextGetter);
            return req;
        });

        return new ResearchRequirementType<>(codec);
    }
}