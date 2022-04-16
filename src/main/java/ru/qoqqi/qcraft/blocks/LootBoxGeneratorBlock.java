package ru.qoqqi.qcraft.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.tileentities.LootBoxGeneratorTileEntity;

public class LootBoxGeneratorBlock extends Block {
	
	private static final VoxelShape SHAPE = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
	
	public LootBoxGeneratorBlock(Properties properties) {
		super(properties);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos blockPos, @Nonnull PlayerEntity player, @Nonnull Hand hand, @Nonnull BlockRayTraceResult hit) {
		TileEntity tileEntity = world.getTileEntity(blockPos);
		
		if (!(tileEntity instanceof LootBoxGeneratorTileEntity)) {
			return ActionResultType.FAIL;
		}
		
		if (hand != Hand.MAIN_HAND) {
			return ActionResultType.PASS;
		}
		
		return ((LootBoxGeneratorTileEntity) tileEntity).onBlockActivated(player);
	}
	
	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		
		TileEntity tileEntity = world.getTileEntity(pos);
		
		if (placer instanceof PlayerEntity && tileEntity instanceof LootBoxGeneratorTileEntity) {
			((LootBoxGeneratorTileEntity) tileEntity).onBlockPlacedBy((PlayerEntity) placer);
		}
	}
	
	@Override
	public void animateTick(@Nonnull BlockState stateIn, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random random) {
		super.animateTick(stateIn, world, pos, random);
		
		TileEntity abstractTileEntity = world.getTileEntity(pos);
		
		if (!(abstractTileEntity instanceof LootBoxGeneratorTileEntity)) {
			return;
		}
		
		LootBoxGeneratorTileEntity tileEntity = (LootBoxGeneratorTileEntity) abstractTileEntity;
		
		if (!tileEntity.isActive()) {
			return;
		}
		
		if (tileEntity.getAge() % 10 == 0) {
			spawnParticle(world, pos, random, 0.3f);
		}
		
		if (!tileEntity.getItemStack().isEmpty() || random.nextFloat() <= tileEntity.getProgress()) {
			spawnParticle(world, pos, random, 0.25f);
		}
	}
	
	private void spawnParticle(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull Random random, float spread) {
		double x = pos.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * spread;
		double y = pos.getY() + 0.75;
		double z = pos.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * spread;
		double ySpeed = random.nextFloat() * 0.04;
		
		world.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, ySpeed, 0);
	}
	
	@SuppressWarnings("deprecation")
	public boolean isTransparent(@Nonnull BlockState state) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull ISelectionContext context) {
		return SHAPE;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new LootBoxGeneratorTileEntity();
	}
}
