package dev.wolfieboy09.researchtree.rewards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;
import dev.wolfieboy09.researchtree.core.ResearchRewardType;
import dev.wolfieboy09.researchtree.registries.RTRewardTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Attr;

public record AttributeModifierReward(
        Holder<Attribute> attribute,
        ResourceLocation modifierId,
        double amount,
        AttributeModifier.Operation operation
) implements ResearchReward {
    public static final MapCodec<AttributeModifierReward> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeModifierReward::attribute),
                    ResourceLocation.CODEC.fieldOf("modifier_id").forGetter(AttributeModifierReward::modifierId),
                    Codec.FLOAT.fieldOf("amount").forGetter(r -> (float) r.amount),
                    AttributeModifier.Operation.CODEC.optionalFieldOf("operation", AttributeModifier.Operation.ADD_VALUE).forGetter(AttributeModifierReward::operation)
            ).apply(instance, AttributeModifierReward::new)
    );

    @Override
    public void grant(Player player, Level level) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(modifierId);

            AttributeModifier modifier = new AttributeModifier(
                    modifierId,
                    amount,
                    operation
            );
            instance.addPermanentModifier(modifier);
        }
    }

    @Override
    public Component getDisplayText() {
        String operationKey = switch (operation) {
            case ADD_VALUE -> "add";
            case ADD_MULTIPLIED_BASE -> "multiply_base";
            case ADD_MULTIPLIED_TOTAL -> "multiply_total";
        };

        return Component.translatable(
                "reward.researchtree.attribute." + operationKey,
                amount,
                Component.translatable(attribute.value().getDescriptionId())
        );
    }

    @Override
    public @NotNull ResearchRewardType<?> getType() {
        return RTRewardTypes.ATTRIBUTE_MODIFIER.get();
    }
}