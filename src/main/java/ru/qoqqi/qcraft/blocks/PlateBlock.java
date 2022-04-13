package ru.qoqqi.qcraft.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.Objects;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.util.RefltectionUtils;

public class PlateBlock extends PaneBlock {
	
	public static final BooleanProperty IS_COVERING = BooleanProperty.create("covering");
	
	protected static final VoxelShape HORIZONTAL_SHAPE =
			Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
	
	private static final float nodeWidth = 2.0f;
	
	private static final float extensionWidth = 2.0f;
	
	private static final float nodeHeight = 16.0f;
	
	private static final float extensionHeight = 16.0f;
	
	private static final float collisionY = 16.0f;
	
	public PlateBlock(AbstractBlock.Properties builder) {
		super(builder);
		
		RefltectionUtils.setDeclaredFinal(FourWayBlock.class, this, "collisionShapes",
				this.makeShapes(nodeWidth, extensionWidth, collisionY, 0.0F, collisionY));
		
		RefltectionUtils.setDeclaredFinal(FourWayBlock.class, this, "shapes",
				this.makeShapes(nodeWidth, extensionWidth, nodeHeight, 0.0F, extensionHeight));
		
		setDefaultState(getDefaultState().with(IS_COVERING, false));
	}
	
	@Nonnull
	public VoxelShape getShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
		if (state.get(IS_COVERING)) {
			return HORIZONTAL_SHAPE;
		}
		
		return super.getShape(state, worldIn, pos, context);
	}
	
	@Nonnull
	public VoxelShape getCollisionShape(BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
		if (state.get(IS_COVERING)) {
			return HORIZONTAL_SHAPE;
		}
		
		return super.getCollisionShape(state, worldIn, pos, context);
	}
	
	@Override
	protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(IS_COVERING);
	}
	
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IBlockReader blockReader = context.getWorld();
		BlockPos blockPos = context.getPos();
		FluidState fluidState = context.getWorld().getFluidState(context.getPos());
		
		BlockPos north = blockPos.north();
		BlockPos south = blockPos.south();
		BlockPos west = blockPos.west();
		BlockPos east = blockPos.east();
		
		BlockState state = this.getDefaultState()
				.with(NORTH, this.canAttachTo(north, blockReader, Direction.SOUTH))
				.with(SOUTH, this.canAttachTo(south, blockReader, Direction.NORTH))
				.with(WEST, this.canAttachTo(west, blockReader, Direction.EAST))
				.with(EAST, this.canAttachTo(east, blockReader, Direction.WEST))
				.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
		
		if (context.hasSecondaryUseForPlayer()) {
			state = state.with(IS_COVERING, true);
		}
		
		return state;
	}
	
	@Nonnull
	public BlockState updatePostPlacement(@Nonnull BlockState state, @Nonnull Direction facing,
	                                      @Nonnull BlockState facingState, @Nonnull IWorld world,
	                                      @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
		
		state = super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
		
		if (facing.getAxis().isHorizontal()) {
			BooleanProperty property = FACING_TO_PROPERTY_MAP.get(facing);
			boolean canAttachTo = this.canAttachTo(facingPos, world, facing.getOpposite());
			
			state = state.with(property, canAttachTo);
		}
		
		return state;
	}
	
	public boolean canAttachTo(BlockPos blockPos, IBlockReader blockReader, Direction direction) {
		BlockState blockState = blockReader.getBlockState(blockPos);
		Block block = blockState.getBlock();
		boolean solidSide = blockState.isSolidSide(blockReader, blockPos, direction);
		
		return !cannotAttach(block) && solidSide
				|| (block instanceof PaneBlock && !blockState.get(IS_COVERING))
				|| block.isIn(BlockTags.WALLS);
	}
}