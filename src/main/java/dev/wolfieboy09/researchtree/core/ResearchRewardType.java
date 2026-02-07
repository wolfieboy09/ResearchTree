package dev.wolfieboy09.researchtree.core;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.api.research.ResearchReward;

public record ResearchRewardType<T extends ResearchReward>(MapCodec<T> codec) {}