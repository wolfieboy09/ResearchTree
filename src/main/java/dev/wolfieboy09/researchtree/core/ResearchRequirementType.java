package dev.wolfieboy09.researchtree.core;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.api.research.ResearchRequirement;

public record ResearchRequirementType<T extends ResearchRequirement<?>>(MapCodec<T> codec) {
}