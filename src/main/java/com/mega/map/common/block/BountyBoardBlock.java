package com.mega.map.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BountyBoardBlock extends HorizontalDirectionalBlock implements IItemBlock {
    static VoxelShape makeShapeN() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-1, 0, 0.875, -0.875, 1.5, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.875, 0, 0.875, 2, 1.5, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 1.375, 0.875, 1.875, 1.5, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 0, 0.875, 1.875, 0.125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 0.125, 0.9375, 1.875, 1.375, 1), BooleanOp.OR);
        return shape;
    }
    static VoxelShape makeShapeE(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 1.875, 0.125, 1.5, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, -0.875, 0.125, 0.125, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.125, -0.875, 0.0625, 1.375, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.375, -0.875, 0.125, 1.5, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0, -1, 0.125, 1.5, -0.875), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShapeW(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.875, 0, -1, 1, 1.5, -0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 1.875, 1, 1.5, 2), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 1.375, -0.875, 1, 1.5, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.9375, 0.125, -0.875, 1, 1.375, 1.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.875, 0, -0.875, 1, 0.125, 1.875), BooleanOp.OR);

        return shape;
    }
    static VoxelShape makeShapeS(){
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-1, 0, 0, -0.875, 1.5, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(1.875, 0, 0, 2, 1.5, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 1.375, 0, 1.875, 1.5, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 0.125, 0, 1.875, 1.375, 0.0625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 0, 0, 1.875, 0.125, 0.125), BooleanOp.OR);

        return shape;
    }
    protected static final VoxelShape NORTH_SHAPE = makeShapeN();
    protected static final VoxelShape EAST_SHAPE = makeShapeE();
    protected static final VoxelShape WEST_SHAPE = makeShapeW();
    protected static final VoxelShape SOUTH_SHAPE = makeShapeS();
    public BountyBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51305_) {
        p_51305_.add(FACING);
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull PathComputationType pathComputationType) {
        return false;
    }
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        Direction direction = blockState.getValue(FACING);
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> EAST_SHAPE;
        };
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction.getOpposite());
    }
    @Override
    public Item.Properties asItemProperties() {
        return new Item.Properties().stacksTo(64);
    }

}
