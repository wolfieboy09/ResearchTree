package dev.wolfieboy09.researchtree.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class RTServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GLOBAL_MAX_ACTIVE_RESEARCH = BUILDER
            .comment(
                    "Maximum number of research nodes a player may have actively in-progress at once,",
                    "counted across ALL categories combined.",
                    "Set to 1 for the classic 'only one research at a time' behavior.",
                    "Set to 0 (or negative) to disable this global cap and rely only on",
                    "'defaultCategoryMaxActiveResearch' / per-category overrides below."
            )
            .defineInRange("globalMaxActiveResearch", 1, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue DEFAULT_CATEGORY_MAX_ACTIVE_RESEARCH = BUILDER
            .comment(
                    "Default maximum number of research nodes a player may have actively in-progress",
                    "at once WITHIN A SINGLE CATEGORY, used for any category that doesn't set its own",
                    "\"max_active_research\" value.",
                    "Set to 0 (or negative) to disable the per-category cap by default, so only",
                    "'globalMaxActiveResearch' applies unless a category overrides this."
            )
            .defineInRange("defaultCategoryMaxActiveResearch", 1, 0, Integer.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
