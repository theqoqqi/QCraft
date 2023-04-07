package ru.qoqqi.qcraft.spawners;

import com.mojang.logging.LogUtils;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CustomNaturalSpawner {
	
	private static final Logger LOGGER = LogUtils.getLogger();
	
	private static final Map<SpawnerType, SpawnerTypeHandler> allSpawners = new HashMap<>();
	
	private static final ExecutorService executorService = Executors.newScheduledThreadPool(10);
	
	public static void addSpawns(SpawnerType spawnerType,
	                             Holder<Biome> biome,
	                             List<MobSpawnSettings.SpawnerData> spawns) {
		
		var spawner = allSpawners
				.computeIfAbsent(spawnerType, spawnerType.spawnerTypeHandlerFactory);
		
		spawner.addSpawns(biome, spawns);
	}
	
	@SubscribeEvent
	public static void onLoadChunk(final ChunkEvent.Load event) {
		if (!event.isNewChunk()) {
			return;
		}
		
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}
		
		var chunk = (LevelChunk) event.getChunk();
		var chunkCache = level.getChunkSource();
		
		onChunkReady(chunkCache, chunk, () -> {
			allSpawners.values().forEach(spawnerTypeHandler -> {
				if (spawnerTypeHandler.getSpawnerType().spawnsOnChunkGeneration) {
					spawnForChunk(chunk, level, spawnerTypeHandler);
				}
			});
		});
	}
	
	private static void onChunkReady(ServerChunkCache chunkCache, LevelChunk chunk, Runnable action) {
		executorService.submit(() -> {
			var chunkPos = chunk.getPos();
			var future = chunkCache.getChunkFuture(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
			
			future.thenAccept(either -> {
				either.left().ifPresent(chunkAccess -> {
					action.run();
				});
			});
		});
	}
	
	private static void spawnForChunk(LevelChunk chunk, ServerLevel level, SpawnerTypeHandler handler) {
		var server = level.getServer();
		var spawnEnemies = server.isSpawningMonsters();
		var spawnFriendlies = server.isSpawningAnimals();
		
		handler.spawnForChunk(level, chunk, spawnEnemies, spawnFriendlies);
	}
}
