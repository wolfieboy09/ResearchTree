package dev.wolfieboy09.researchtree.content.block;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.content.blockentity.ResearchTableBlockEntity;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.network.SyncResearchDataPacket;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import dev.wolfieboy09.researchtree.registries.RTBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<ResearchTableBlock> CODEC = simpleCodec(ResearchTableBlock::new);

    public ResearchTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResearchTableBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            PlayerResearchData data = player.getData(RTAttachments.RESEARCH_DATA);

            if (data.hasResearchTable()) {
                player.sendSystemMessage(Component.translatable("message.researchtree.already_has_table"));
                return;
            }

            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity blockEntity) {
                blockEntity.setOwner(player.getUUID());
                data.setResearchTablePos(level.dimension(), pos);
                PacketDistributor.sendToPlayer(player, new SyncResearchDataPacket(data.save()));
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity blockEntity) {
                if (blockEntity.getOwner() != null && blockEntity.getOwner().equals(player.getUUID())) {
                    PlayerResearchData data = serverPlayer.getData(RTAttachments.RESEARCH_DATA);
                    data.clearResearchTablePos();

                    PacketDistributor.sendToPlayer(serverPlayer, new SyncResearchDataPacket(data.save()));
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, RTBlockEntities.RESEARCH_TABLE.get(), ResearchTableBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}