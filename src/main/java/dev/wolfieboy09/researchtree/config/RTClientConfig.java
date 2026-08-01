package dev.wolfieboy09.researchtree.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RTClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue smoothScrollingEnabled;

    public static ModConfigSpec SPEC;

    static {
        smoothScrollingEnabled = BUILDER
                .comment("Controls smooth scrolling in the research tree screen")
                .translation("config.researchtree.smoothScrollingEnabled")
                .define("enableSmoothScrolling", true);

        SPEC = BUILDER.build();
    }
}
