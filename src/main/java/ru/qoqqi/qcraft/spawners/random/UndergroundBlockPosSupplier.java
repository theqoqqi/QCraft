package ru.qoqqi.qcraft.spawners.random;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UndergroundBlockPosSupplier extends SpawnPosSupplier {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	private UndergroundBlockPosSupplier(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos) {
		super(level, entityType, chunkPos);
	}
	
	@Override
	protected BlockPos prepareBlockPos(int x, int z) {
		var positions = getNonCollidingPositions(level, entityType, x, z);
		
		if (positions.isEmpty()) {
			return null;
		}
		
		var randomIndex = random.nextInt(positions.size());
		var blockPos = positions.get(randomIndex);
		
		return fixForPathfinding(level, entityType, blockPos);
	}
	
	protected static List<BlockPos> getNonCollidingPositions(LevelReader level, EntityType<?> entityType, int x, int z) {
		var minBuildHeight = level.getMinBuildHeight();
		var heightmapType = SpawnPlacements.getHeightmapType(entityType);
		var blockPos = getTopBlockPos(level, heightmapType, x, z);
		var positions = new ArrayList<BlockPos>();
		
		do {
			blockPos.move(Direction.DOWN);
		} while (!isSolid(level, blockPos) && blockPos.getY() > minBuildHeight);
		
		blockPos.move(Direction.DOWN);
		
		while (blockPos.getY() > minBuildHeight) {
			do {
				blockPos.move(Direction.DOWN);
			} while (isSolid(level, blockPos));
			
			do {
				blockPos.move(Direction.DOWN);
			} while (!isSolid(level, blockPos) && blockPos.getY() > minBuildHeight);
			
			if (blockPos.getY() > minBuildHeight) {
				positions.add(blockPos.above().immutable());
			}
		}
		
		return positions;
	}
	
	private static boolean isSolid(LevelReader level, BlockPos blockPos) {
		var blockState = level.getBlockState(blockPos);
		
		return !blockState.isAir() && blockState.getFluidState().isEmpty();
	}
	
	public static UndergroundBlockPosSupplier inChunk(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos) {
		return new UndergroundBlockPosSupplier(level, entityType, chunkPos);
	}
}
