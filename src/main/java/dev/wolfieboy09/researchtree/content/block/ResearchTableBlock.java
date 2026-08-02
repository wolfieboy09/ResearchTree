package dev.wolfieboy09.researchtree.content.block;

import com.mojang.serialization.MapCodec;
import dev.wolfieboy09.researchtree.content.blockentity.ResearchTableBlockEntity;
import dev.wolfieboy09.researchtree.data.PlayerResearchData;
import dev.wolfieboy09.researchtree.network.OpenResearchScreenPacket;
import dev.wolfieboy09.researchtree.network.SyncResearchDataPacket;
import dev.wolfieboy09.researchtree.registries.RTAttachments;
import dev.wolfieboy09.researchtree.registries.RTBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.EnumMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchTableBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final MapCodec<ResearchTableBlock> CODEC = simpleCodec(ResearchTableBlock::new);
    private static final Map<Direction, VoxelShape> SHAPES_BY_FACING = buildShapesByFacing();


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
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return entity instanceof Player player
                && level.getBlockEntity(pos) instanceof ResearchTableBlockEntity table
                && table.getOwner().equals(player.getUUID()) || super.canEntityDestroy(state, level, pos, entity);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            PlayerResearchData data = player.getData(RTAttachments.RESEARCH_DATA);

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

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (level.getBlockEntity(pos) instanceof ResearchTableBlockEntity) {
                PacketDistributor.sendToPlayer(serverPlayer, new OpenResearchScreenPacket());
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }


    private static Map<Direction, VoxelShape> buildShapesByFacing() {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);

        var base = Block.box(0, 0, 0, 16, 1, 16);
        var stand = Block.box(7, 1, 7, 9, 14, 9);
        var common = Shapes.or(base, stand);

        VoxelShape topPlate = Shapes.empty();
        int steps = 8;
        double totalHeight = 6.5;
        double totalDepth = 13;
        double stepHeight = totalHeight / steps;
        double stepDepth = totalDepth / steps;
        double plateThicknessZ = 4;

        for (int i = 0; i < steps; i++) {
            double y0 = 10.5 + i * stepHeight;
            double y1 = y0 + stepHeight;
            double z0 = i * stepDepth;
            double z1 = Math.min(z0 + plateThicknessZ, 16);

            topPlate = Shapes.or(topPlate, Block.box(0, y0, z0, 16, y1, z1));
        }

        var sideBase = Block.box(14, 1, 7, 16, 7, 9);
        var sideHead = Block.box(14, 7, 6, 16, 11, 10);
        var totalSide = Shapes.or(sideBase, sideHead);

        var totalSide2 = rotateVoxelShape(totalSide, Direction.EAST);
        var totalSide3 = rotateVoxelShape(totalSide, Direction.SOUTH);
        var sides = Shapes.or(totalSide, totalSide2, totalSide3);

        VoxelShape canonical = Shapes.or(common, topPlate, sides);

        for (Direction facing : BlockStateProperties.HORIZONTAL_FACING.getPossibleValues()) {
            map.put(facing, rotateVoxelShape(canonical, facing));
        }

        return map;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_BY_FACING.get(state.getValue(FACING));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }


    // rotateVoxelShape and rotatePointAroundCenter is taken from
    // https://github.com/wolfieboy09/QTech/blob/main/src/main/java/dev/wolfieboy09/qtech/api/util/VoxelUtil.java
    private static VoxelShape rotateVoxelShape(VoxelShape shape, Direction direction) {
        return Shapes.or(Shapes.empty(), shape.toAabbs().stream()
                .map(box -> Shapes.create(new AABB(
                        rotatePointAroundCenter(box.minX, box.minY, box.minZ, direction),
                        rotatePointAroundCenter(box.maxX, box.maxY, box.maxZ, direction)
                ))).toArray(VoxelShape[]::new));
    }

    private static Vec3 rotatePointAroundCenter(double x, double y, double z, Direction direction) {
        return switch (direction) {
            case NORTH -> new Vec3(x, y, z);
            case SOUTH -> new Vec3(1 - x, y, 1 - z);
            case EAST -> new Vec3(1 - z, y, x);
            case WEST -> new Vec3(z, y, 1 - x);
            case DOWN -> new Vec3(x, z, 1 - y);
            case UP -> new Vec3(x, 1 - z, y);
        };
    }
}