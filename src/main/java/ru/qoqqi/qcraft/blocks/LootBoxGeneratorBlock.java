package ru.qoqqi.qcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.blockentities.LootBoxGeneratorBlockEntity;
import ru.qoqqi.qcraft.blockentities.ModBlockEntityTypes;

public class LootBoxGeneratorBlock extends BaseEntityBlock {
	
	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
	
	public static final Vec3 TABLE_CENTER = new Vec3(0.5, 0.75, 0.5);
	
	public LootBoxGeneratorBlock(Properties properties) {
		super(properties);
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos blockPos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		
		if (!(blockEntity instanceof LootBoxGeneratorBlockEntity)) {
			return InteractionResult.FAIL;
		}
		
		if (hand != InteractionHand.MAIN_HAND) {
			return InteractionResult.CONSUME;
		}
		
		return ((LootBoxGeneratorBlockEntity) blockEntity).onBlockActivated(player);
	}
	
	@Override
	public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
		return super.canHarvestBlock(state, level, pos, player)
				&& canBeBrokenBy(level, pos, player);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public float getDestroyProgress(@Nonnull BlockState state, @Nonnull Player player, @Nonnull BlockGetter level, @Nonnull BlockPos pos) {
		if (!canBeBrokenBy(level, pos, player)) {
			return -1f;
		}
		
		return super.getDestroyProgress(state, player, level, pos);
	}
	
	public boolean canBeBrokenBy(@Nonnull BlockGetter level, BlockPos pos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		
		if (!(blockEntity instanceof LootBoxGeneratorBlockEntity)) {
			return true;
		}
		
		UUID ownerUuid = ((LootBoxGeneratorBlockEntity) blockEntity).getOwnerUuid();
		
		return (ownerUuid == null || ownerUuid.equals(player.getUUID()));
	}
	
	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		
		BlockEntity blockEntity = level.getBlockEntity(pos);
		
		if (placer instanceof ServerPlayer && blockEntity instanceof LootBoxGeneratorBlockEntity) {
			((LootBoxGeneratorBlockEntity) blockEntity).onBlockPlacedBy((Player) placer);
		}
	}
	
	@Override
	public void animateTick(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource random) {
		super.animateTick(state, level, pos, random);
		
		BlockEntity abstractBlockEntity = level.getBlockEntity(pos);
		
		if (!(abstractBlockEntity instanceof LootBoxGeneratorBlockEntity blockEntity)) {
			return;
		}
		
		if (!blockEntity.isActive()) {
			return;
		}
		
		if (blockEntity.getAge() % 10 == 0) {
			spawnParticle(level, pos, random, 0.3f);
		}
		
		if (blockEntity.getAge() % 2 == 0) {
			if (blockEntity.hasItem() || random.nextFloat() <= blockEntity.getProgress()) {
				spawnParticle(level, pos, random, 0.25f);
			}
		}
	}
	
	private void spawnParticle(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull RandomSource random, float spread) {
		double x = pos.getX() + TABLE_CENTER.x + (random.nextDouble() - random.nextDouble()) * spread;
		double y = pos.getY() + TABLE_CENTER.y;
		double z = pos.getZ() + TABLE_CENTER.z + (random.nextDouble() - random.nextDouble()) * spread;
		double ySpeed = random.nextFloat() * 0.04;
		
		level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, ySpeed, 0);
	}
	
	@SuppressWarnings("deprecation")
	public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter levelIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return SHAPE;
	}
	
	@NotNull
	public RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new LootBoxGeneratorBlockEntity(pos, state);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return type == ModBlockEntityTypes.LOOT_BOX_GENERATOR.get()
				? (pLevel, pPos, pState, pBlockEntity) -> ((LootBoxGeneratorBlockEntity) pBlockEntity).tick()
				: null;
	}
}
