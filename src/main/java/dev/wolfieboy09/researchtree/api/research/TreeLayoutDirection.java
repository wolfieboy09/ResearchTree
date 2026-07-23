package dev.wolfieboy09.researchtree.api.research;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Controls the axis that the auto layout engine grows a research category's tree along.
 * <p>
 * {@link #TOP_DOWN} lays roots out at the top and grows prerequisite chains downward (rows = tiers).
 * {@link #LEFT_RIGHT} lays roots out on the left and grows prerequisite chains rightward (columns = tiers).
 */
public enum TreeLayoutDirection implements StringRepresentable {
    TOP_DOWN("top_down"),
    LEFT_RIGHT("left_right");

    public static final Codec<TreeLayoutDirection> CODEC = StringRepresentable.fromEnum(TreeLayoutDirection::values);

    private final String name;

    TreeLayoutDirection(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public boolean isHorizontal() {
        return this == LEFT_RIGHT;
    }
}
