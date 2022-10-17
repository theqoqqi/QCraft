package ru.qoqqi.qcraft.blocks;

import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class PlateBlock extends CrossCollisionBlock {
	
	public static final BooleanProperty IS_COVERING = BooleanProperty.create("covering");
	
	protected static final VoxelShape HORIZONTAL_SHAPE =
			Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
	
	private static final float nodeWidth = 2.0f;
	
	private static final float extensionWidth = 2.0f;
	
	private static final float nodeHeight = 16.0f;
	
	private static final float extensionHeight = 16.0f;
	
	private static final float collisionY = 16.0f;
	
	public PlateBlock(BlockBehaviour.Properties builder) {
		super(nodeWidth, extensionWidth, nodeHeight, extensionHeight, collisionY, builder);
		
		registerDefaultState(
				getStateDefinition().any()
						.setValue(NORTH, false)
						.setValue(SOUTH, false)
						.setValue(WEST, false)
						.setValue(EAST, false)
						.setValue(WATERLOGGED, false)
						.setValue(IS_COVERING, false)
		);
	}
	
	@Override
	@Nonnull
	public VoxelShape getShape(BlockState state, @Nonnull BlockGetter levelIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		if (state.getValue(IS_COVERING)) {
			return HORIZONTAL_SHAPE;
		}
		
		return super.getShape(state, levelIn, pos, context);
	}
	
	@Override
	@Nonnull
	public VoxelShape getCollisionShape(BlockState state, @Nonnull BlockGetter levelIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		if (state.getValue(IS_COVERING)) {
			return HORIZONTAL_SHAPE;
		}
		
		return super.getCollisionShape(state, levelIn, pos, context);
	}
	
	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED, IS_COVERING);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelAccessor blockReader = context.getLevel();
		BlockPos blockPos = context.getClickedPos();
		FluidState fluidState = context.getLevel().getFluidState(blockPos);
		
		BlockPos north = blockPos.north();
		BlockPos south = blockPos.south();
		BlockPos west = blockPos.west();
		BlockPos east = blockPos.east();
		
		return this.getStateDefinition().any()
				.setValue(NORTH, this.canAttachTo(north, blockReader, Direction.SOUTH))
				.setValue(SOUTH, this.canAttachTo(south, blockReader, Direction.NORTH))
				.setValue(WEST, this.canAttachTo(west, blockReader, Direction.EAST))
				.setValue(EAST, this.canAttachTo(east, blockReader, Direction.WEST))
				.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
				.setValue(IS_COVERING, context.isSecondaryUseActive());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@NotNull
	public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction,
	                              @NotNull BlockState neighborState, @NotNull LevelAccessor level,
	                              @NotNull BlockPos currentPos, @NotNull BlockPos neighborPos) {
		
		state = super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
		
		if (direction.getAxis().isHorizontal()) {
			BooleanProperty property = PROPERTY_BY_DIRECTION.get(direction);
			boolean canAttachTo = this.canAttachTo(neighborPos, level, direction.getOpposite());
			
			state = state.setValue(property, canAttachTo);
		}
		
		return state;
	}
	
	public boolean canAttachTo(BlockPos blockPos, LevelAccessor blockReader, Direction direction) {
		BlockState blockState = blockReader.getBlockState(blockPos);
		Block block = blockState.getBlock();
		boolean solidSide = blockState.isFaceSturdy(blockReader, blockPos, direction);
		
		return !isExceptionForConnection(blockState) && solidSide
				|| (block instanceof CrossCollisionBlock && !isCovering(blockState))
				|| isInTag(block, BlockTags.WALLS)
				|| isInTag(block, BlockTags.FENCE_GATES);
	}
	
	private boolean isCovering(BlockState blockState) {
		return blockState.getBlock() instanceof PlateBlock && blockState.getValue(IS_COVERING);
	}
	
	private static boolean isInTag(Block block, TagKey<Block> tagKey) {
		ITagManager<Block> tags = ForgeRegistries.BLOCKS.tags();
		
		return tags != null && tags.getTag(tagKey).contains(block);
	}
}