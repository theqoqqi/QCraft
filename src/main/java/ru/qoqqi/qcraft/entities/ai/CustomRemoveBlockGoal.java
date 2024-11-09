package ru.qoqqi.qcraft.entities.ai;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.function.Predicate;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CustomRemoveBlockGoal extends MoveToBlockGoal {

	private static final int WAIT_AFTER_BLOCK_FOUND = 20;

	private final Predicate<BlockState> blockPredicate;

	private final Mob removerMob;

	private int ticksSinceReachedGoal;

	private final boolean withDrops;

	public CustomRemoveBlockGoal(Predicate<BlockState> blockPredicate, PathfinderMob removerMob, double speedModifier, int searchRange, boolean withDrops) {
		super(removerMob, speedModifier, 24, searchRange);
		this.blockPredicate = blockPredicate;
		this.removerMob = removerMob;
		this.withDrops = withDrops;
	}

	public boolean canUse() {
		if (!ForgeEventFactory.getMobGriefingEvent(removerMob.level(), removerMob)) {
			return false;
		}

		if (nextStartTick > 0) {
			--nextStartTick;
			return false;
		}

		if (!tryFindBlock()) {
			nextStartTick = nextStartTick(mob);
			return false;
		}

		nextStartTick = reducedTickDelay(WAIT_AFTER_BLOCK_FOUND);
		return true;
	}

	private boolean tryFindBlock() {
		if (isValidTarget(mob.level(), blockPos)) {
			return true;
		}

		return this.findNearestBlock();
	}

	public void stop() {
		super.stop();
		this.removerMob.fallDistance = 1.0F;
	}

	public void start() {
		super.start();
		this.ticksSinceReachedGoal = 0;
	}

	@SuppressWarnings("unused")
	public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos pos) {
	}

	@SuppressWarnings("unused")
	public void playBreakSound(Level level, BlockPos pos) {
	}

	public void tick() {
		super.tick();

		var level = removerMob.level();
		var targetBlockPos = blockPos;
		var random = removerMob.getRandom();

		if (!isReachedTarget()) {
			return;
		}

		var targetX = (double) targetBlockPos.getX() + 0.5D;
		var targetZ = (double) targetBlockPos.getZ() + 0.5D;
		var targetY = targetBlockPos.getY();

		if (ticksSinceReachedGoal > 0) {
			var delta = removerMob.getDeltaMovement();

			removerMob.setDeltaMovement(delta.x, 0.25D, delta.z);

			if (!level.isClientSide) {
				createHitParticles((ServerLevel) level, random, targetX, targetZ, targetY);
			}
		}

		if (ticksSinceReachedGoal % 2 == 0) {
			var delta = removerMob.getDeltaMovement();

			removerMob.setDeltaMovement(delta.x, -0.25D, delta.z);

			if (ticksSinceReachedGoal % 6 == 0) {
				playDestroyProgressSound(level, blockPos);
			}
		}

		if (ticksSinceReachedGoal > 60) {
			if (withDrops) {
				createDrops(targetBlockPos);
			}

			destroyBlock(level, targetBlockPos);

			if (!level.isClientSide) {
				for (int i = 0; i < 5; ++i) {
					createBreakParticles((ServerLevel) level, random, targetX, targetZ, targetY);
				}

				playBreakSound(level, targetBlockPos);
			}
		}

		++ticksSinceReachedGoal;
	}

	protected void destroyBlock(Level level, BlockPos targetBlockPos) {
		level.removeBlock(targetBlockPos, false);
	}

	private void createDrops(BlockPos blockPos) {
		if (removerMob.level().isClientSide) {
			return;
		}

		var level = (ServerLevel) removerMob.level();
		var blockState = level.getBlockState(blockPos);
		var blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
		var lootParamsBuilder = new LootParams.Builder(level)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
				.withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
				.withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);

		ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositions = new ObjectArrayList<>();

		blockState.spawnAfterBreak(level, blockPos, ItemStack.EMPTY, false);
		blockState.getDrops(lootParamsBuilder).forEach((itemStack) -> {
			addBlockDrops(dropPositions, itemStack, blockPos.immutable());
		});

		dropPositions.forEach(entry -> {
			Block.popResource(level, entry.getSecond(), entry.getFirst());
		});
	}

	private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> dropPositions, ItemStack pStack, BlockPos pPos) {
		int dropCount = dropPositions.size();

		for (int i = 0; i < dropCount; ++i) {
			var pair = dropPositions.get(i);
			var itemStack = pair.getFirst();

			if (ItemEntity.areMergable(itemStack, pStack)) {
				var mergedItemStack = ItemEntity.merge(itemStack, pStack, 16);

				dropPositions.set(i, Pair.of(mergedItemStack, pair.getSecond()));

				if (pStack.isEmpty()) {
					return;
				}
			}
		}

		dropPositions.add(Pair.of(pStack, pPos));
	}

	private static void createHitParticles(ServerLevel level, RandomSource random, double targetX, double targetZ, double targetY) {
		var particleOption = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.EGG));

		var emitRadius = 0.08D;
		var xOffset = ((double) random.nextFloat() - 0.5D) * emitRadius;
		var yOffset = ((double) random.nextFloat() - 0.5D) * emitRadius;
		var zOffset = ((double) random.nextFloat() - 0.5D) * emitRadius;

		level.sendParticles(particleOption, targetX, targetY + 0.1, targetZ, 1, xOffset, yOffset, zOffset, 0.15);
	}

	private static void createBreakParticles(ServerLevel level, RandomSource random, double targetX, double targetZ, double targetY) {
		var xOffset = random.nextGaussian() * 0.02D;
		var yOffset = random.nextGaussian() * 0.02D;
		var zOffset = random.nextGaussian() * 0.02D;

		level.sendParticles(ParticleTypes.POOF, targetX, targetY, targetZ, 1, xOffset, yOffset, zOffset, 0.15);
	}

	protected boolean isValidTarget(LevelReader level, BlockPos blockPos) {
		int sectionX = SectionPos.blockToSectionCoord(blockPos.getX());
		int sectionY = SectionPos.blockToSectionCoord(blockPos.getZ());

		var chunkAccess = level.getChunk(sectionX, sectionY, ChunkStatus.FULL, false);

		if (chunkAccess == null) {
			return false;
		}

		var blockState = chunkAccess.getBlockState(blockPos);

		if (!blockState.canEntityDestroy(level, blockPos, removerMob)) {
			return false;
		}

		return blockPredicate.test(blockState);
	}
}
