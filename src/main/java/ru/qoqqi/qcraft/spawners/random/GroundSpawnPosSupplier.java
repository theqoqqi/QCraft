package ru.qoqqi.qcraft.spawners.random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;

public class GroundSpawnPosSupplier extends SpawnPosSupplier {
	
	private GroundSpawnPosSupplier(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos) {
		super(level, entityType, chunkPos);
	}
	
	@Override
	protected BlockPos prepareBlockPos(int x, int z) {
		var heightmapType = SpawnPlacements.getHeightmapType(entityType);
		var blockPos = getTopBlockPos(level, heightmapType, x, z);
		
		return fixForPathfinding(level, entityType, blockPos);
	}
	
	public static GroundSpawnPosSupplier inChunk(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos) {
		return new GroundSpawnPosSupplier(level, entityType, chunkPos);
	}
}
