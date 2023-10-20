package ru.qoqqi.qcraft.spawners.random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathComputationType;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class SpawnPosSupplier implements Supplier<BlockPos> {
	
	protected final ServerLevel level;
	
	protected final EntityType<?> entityType;
	
	protected final RandomSource random;
	
	protected final BlockPos minBound;
	
	protected final BlockPos maxBound;
	
	protected final BlockPos initial;
	
	private BlockPos current;
	
	private final int maxTries = 64;
	
	public SpawnPosSupplier(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos) {
		this.level = level;
		this.entityType = entityType;
		this.random = level.random;
		this.minBound = chunkPos.getBlockAt(0, 0, 0);
		this.maxBound = chunkPos.getBlockAt(15, 0, 15);
		
		initial = nextRandomPos();
		current = initial;
	}
	
	@Override
	public BlockPos get() {
		if (current == null) {
			current = nextRandomPos();
		}
		
		var result = current;
		
		current = null;
		
		return result;
	}
	
	private BlockPos nextRandomPos() {
		var tries = 0;
		BlockPos randomPos;
		
		do {
			randomPos = getRandomBlockPos();
		} while (++tries < maxTries && randomPos == null);
		
		return clampPosition(entityType, randomPos);
	}
	
	protected BlockPos getRandomBlockPos() {
		if (current == null) {
			var x = minBound.getX() + random.nextInt(16);
			var z = minBound.getZ() + random.nextInt(16);
			
			return prepareBlockPos(x, z);
		}
		
		var x = current.getX() + random.nextInt(5) - random.nextInt(5);
		var z = current.getZ() + random.nextInt(5) - random.nextInt(5);
		
		while (isInBounds(x, z)) {
			x = initial.getX() + random.nextInt(5) - random.nextInt(5);
			z = initial.getZ() + random.nextInt(5) - random.nextInt(5);
		}
		
		return prepareBlockPos(x, z);
	}
	
	protected abstract BlockPos prepareBlockPos(int x, int z);
	
	@NotNull
	protected static BlockPos clampPosition(EntityType<?> entityType, BlockPos blockPos) {
		var width = entityType.getWidth();
		var chunkPos = new ChunkPos(blockPos);
		var minBlockX = chunkPos.getMinBlockX();
		var minBlockZ = chunkPos.getMinBlockZ();
		var x = blockPos.getX();
		var y = blockPos.getY();
		var z = blockPos.getZ();
		var clampedX = Mth.clamp(x, minBlockX + width / 2, minBlockX + 16.0 - width / 2);
		var clampedZ = Mth.clamp(z, minBlockZ + width / 2, minBlockZ + 16.0 - width / 2);
		
		return BlockPos.containing(clampedX, y, clampedZ);
	}
	
	@NotNull
	protected static BlockPos.MutableBlockPos getTopBlockPos(LevelReader level, Heightmap.Types heightmapType, int x, int z) {
		var height = level.getHeight(heightmapType, x, z);
		var blockPos = new BlockPos.MutableBlockPos(x, height, z);
		
		if (level.dimensionType().hasCeiling()) {
			do {
				blockPos.move(Direction.DOWN);
			} while (!level.getBlockState(blockPos).isAir());
			
			do {
				blockPos.move(Direction.DOWN);
			} while (level.getBlockState(blockPos).isAir() && blockPos.getY() > level.getMinBuildHeight());
		}
		
		return blockPos;
	}
	
	@NotNull
	protected static BlockPos fixForPathfinding(LevelReader level, EntityType<?> entityType, BlockPos blockPos) {
		if (SpawnPlacements.getPlacementType(entityType) == SpawnPlacements.Type.ON_GROUND) {
			var belowPos = blockPos.below();
			
			if (level.getBlockState(belowPos).isPathfindable(level, belowPos, PathComputationType.LAND)) {
				return belowPos;
			}
		}
		
		return blockPos.immutable();
	}
	
	private boolean isInBounds(int x, int z) {
		return x >= minBound.getX() && x <= maxBound.getX()
				&& z >= minBound.getZ() && z <= maxBound.getZ();
	}
}
