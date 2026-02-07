package dev.wolfieboy09.researchtree.integration.kubejs.event.handlers;

import dev.latvian.mods.kubejs.error.KubeRuntimeException;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.util.KubeResourceLocation;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.wolfieboy09.researchtree.api.research.ResearchCategory;
import dev.wolfieboy09.researchtree.data.ResearchCategoryManager;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ResearchCategoryModificationJS implements KubeEvent {
    private final transient Map<ResourceLocation, ResearchCategoryBuilder> categoriesToAdd = new HashMap<>();
    private final transient Map<ResourceLocation, Consumer<ResearchCategoryBuilder>> categoriesToModify = new HashMap<>();

    public ResearchCategoryBuilder create(@NotNull KubeResourceLocation id) {
        ResearchCategoryBuilder builder = new ResearchCategoryBuilder(id.wrapped());
        categoriesToAdd.put(id.wrapped(), builder);
        return builder;
    }

    public ResearchCategoryBuilder modify(ResourceLocation id) {
        ResearchCategory existing = ResearchCategoryManager.getCategory(id);
        if (existing == null) {
            throw new KubeRuntimeException("Research category does not exist: " + id);
        }

        ResearchCategoryBuilder builder = new ResearchCategoryBuilder(existing);
        categoriesToModify.put(id, b -> {});
        return builder;
    }

    public boolean exists(ResourceLocation id) {
        return ResearchCategoryManager.hasCategory(id);
    }

    @HideFromJS
    public void registerCategory(ResourceLocation id, ResearchCategoryBuilder category) {
        this.categoriesToAdd.putIfAbsent(id, category);
    }

    @HideFromJS
    public Map<ResourceLocation, ResearchCategoryBuilder> getCategoriesToAdd() {
        return categoriesToAdd;
    }

    @HideFromJS
    public Map<ResourceLocation, Consumer<ResearchCategoryBuilder>> getCategoriesToModify() {
        return categoriesToModify;
    }

    @HideFromJS
    public void clearCache() {
        categoriesToAdd.clear();
        categoriesToModify.clear();
    }
}
