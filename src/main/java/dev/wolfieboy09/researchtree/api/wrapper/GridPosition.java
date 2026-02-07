package dev.wolfieboy09.researchtree.api.wrapper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GridPosition(
        int x,
        int y
) {
    public static final Codec<GridPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(GridPosition::x),
            Codec.INT.fieldOf("y").forGetter(GridPosition::y)
    ).apply(instance, GridPosition::new));
}
