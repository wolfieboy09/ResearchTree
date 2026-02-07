package dev.wolfieboy09.researchtree.registries;

import dev.wolfieboy09.researchtree.ResearchTreeMod;
import dev.wolfieboy09.researchtree.content.block.ResearchTableBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RTBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, ResearchTreeMod.MOD_ID);

    public static final DeferredHolder<Block, ResearchTableBlock> RESEARCH_TABLE =
            BLOCKS.register("research_table", () -> new ResearchTableBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5f)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
            ));
}