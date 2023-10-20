package ru.qoqqi.qcraft.spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SpawnerTypeHandler {
	
	private static final Map<Holder<Biome>, List<MobSpawnSettings.SpawnerData>> allSpawners
			= new HashMap<>();
	
	protected final SpawnerType spawnerType;
	
	public SpawnerTypeHandler(SpawnerType spawnerType) {
		this.spawnerType = spawnerType;
	}
	
	public void addSpawns(Holder<Biome> biome, List<MobSpawnSettings.SpawnerData> spawns) {
		allSpawners.computeIfAbsent(biome, b -> new ArrayList<>()).addAll(spawns);
	}
	
	public void spawnForChunk(ServerLevel level, LevelChunk chunk, boolean spawnFriendlies, boolean spawnMonsters) {
		
		if (!shouldSpawn(spawnFriendlies, spawnMonsters)) {
			return;
		}
		
		var blockPos = getRandomPosWithin(level, chunk);
		
		if (blockPos.getY() > level.getMinBuildHeight()) {
			spawnForPosition(level, blockPos);
		}
	}
	
	protected boolean shouldSpawn(boolean spawnFriendlies, boolean spawnMonsters) {
		return spawnerType.isFriendly ? spawnFriendlies : spawnMonsters;
	}
	
	protected abstract void spawnForPosition(ServerLevel level, BlockPos blockPos);
	
	public SpawnerType getSpawnerType() {
		return spawnerType;
	}
	
	protected WeightedRandomList<MobSpawnSettings.SpawnerData> getMobs(Holder<Biome> biome) {
		var spawners = allSpawners.getOrDefault(biome, List.of());
		
		return WeightedRandomList.create(spawners);
	}
	
	private static BlockPos getRandomPosWithin(Level level, LevelChunk chunk) {
		var chunkPos = chunk.getPos();
		var x = chunkPos.getMinBlockX() + level.random.nextInt(16);
		var z = chunkPos.getMinBlockZ() + level.random.nextInt(16);
		
		var minX = level.getMinBuildHeight();
		var maxY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
		var y = Mth.randomBetweenInclusive(level.random, minX, maxY);
		
		return new BlockPos(x, y, z);
	}
}
