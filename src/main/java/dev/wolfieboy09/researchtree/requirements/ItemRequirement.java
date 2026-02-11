package dev.wolfieboy09.researchtree.requirements;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;
import dev.wolfieboy09.researchtree.core.ResearchRequirementType;
import dev.wolfieboy09.researchtree.registries.RTRequirementTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRequirement implements ResearchRequirement<ItemStack> {
    public static final MapCodec<ItemRequirement> CODEC =
            RecordCodecBuilder.mapCodec(instance ->
                    instance.group(
                            Codec.either(
                                    TagKey.codec(Registries.ITEM),
                                    BuiltInRegistries.ITEM.byNameCodec()
                            ).fieldOf("item").forGetter(r -> r.target),

                            Codec.INT.optionalFieldOf("count", 1).forGetter(r -> r.requiredCount),

                            ComponentSerialization.CODEC
                                    .optionalFieldOf("display_name", Component.empty())
                                    .forGetter(r -> r.displayName)
                    ).apply(instance, ItemRequirement::new)
            );

    private final Either<TagKey<Item>, Item> target;
    private final int requiredCount;
    private final Component displayName;
    private int currentAmount;

    public ItemRequirement(
            Either<TagKey<Item>, Item> target,
            int requiredCount,
            Component displayName
    ) {
        this.target = target;
        this.requiredCount = requiredCount;
        this.displayName = displayName;
        this.currentAmount = 0;
    }

    public ItemRequirement(TagKey<Item> tag, int count) {
        this(
                Either.left(tag),
                count,
                Component.translatable("tag.item." + tag.location().toLanguageKey())
        );
    }

    public ItemRequirement(Item item, int count) {
        this(
                Either.right(item),
                count,
                Component.translatable(item.getDescriptionId())
        );
    }

    @Override
    public boolean accepts(ItemStack stack) {
        if (currentAmount >= requiredCount) return false;

        return target.map(stack::is, stack::is);
    }

    @Override
    public ItemStack consume(Player player, ItemStack stack) {
        if (!accepts(stack)) return stack;

        int needed = requiredCount - currentAmount;
        int toConsume = Math.min(needed, stack.getCount());

        currentAmount += toConsume;

        ItemStack result = stack.copy();
        result.shrink(toConsume);
        return result;
    }

    @Override
    public ItemStack simulateConsume(Player player, ItemStack stack) {
        if (!accepts(stack)) return stack;

        ItemStack result = stack.copy();
        result.shrink(Math.min(requiredCount - currentAmount, stack.getCount()));
        return result;
    }

    @Override
    public boolean isMet(Player player) {
        return currentAmount >= requiredCount;
    }

    @Override
    public float getProgress(Player player) {
        return (float) currentAmount / requiredCount;
    }

    @Override
    public Component getDisplayText() {
        return Component.translatable(
                "requirement.researchtree.item",
                displayName,
                currentAmount,
                requiredCount
        );
    }

    @Override
    public @NotNull ResearchRequirementType<?> getType() {
        return RTRequirementTypes.ITEM.get();
    }
}