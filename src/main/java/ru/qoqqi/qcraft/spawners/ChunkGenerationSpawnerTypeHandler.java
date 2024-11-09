package ru.qoqqi.qcraft.spawners;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.MobSpawnSettings;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import ru.qoqqi.qcraft.spawners.random.SpawnPosSupplier;

class ChunkGenerationSpawnerTypeHandler extends SpawnerTypeHandler {

	private static final Logger LOGGER = LogUtils.getLogger();

	private final float probability;

	private final SpawnPosSupplierFactory spawnPosSupplierFactory;

	ChunkGenerationSpawnerTypeHandler(SpawnerType spawnerType, float probability, SpawnPosSupplierFactory spawnPosSupplierFactory) {
		super(spawnerType);
		this.probability = probability;
		this.spawnPosSupplierFactory = spawnPosSupplierFactory;
	}

	@Override
	protected void spawnForPosition(ServerLevel level, BlockPos blockPos) {
		var biome = level.getBiome(blockPos);
		var chunkPos = new ChunkPos(blockPos);
		var random = level.random;

		var weightedRandomList = getMobs(biome);

		if (weightedRandomList.isEmpty()) {
			return;
		}

		while (random.nextFloat() < probability) {
			var optional = weightedRandomList.getRandom(random);

			if (optional.isEmpty()) {
				continue;
			}

			SpawnGroupData spawnGroupData = null;
			var spawnerData = optional.get();
			var count = Mth.randomBetweenInclusive(random, spawnerData.minCount, spawnerData.maxCount);

			var randomPosSupplier = spawnPosSupplierFactory.create(level, spawnerData.type, chunkPos);

			for (var i = 0; i < count; ++i) {
				var mobSpawned = false;

				for (int j = 0; !mobSpawned && j < 4; ++j) {
					var randomPos = randomPosSupplier.get();
					var entity = trySpawnEntity(level, random, spawnerData, randomPos);

					if (entity == null) {
						continue;
					}

					if (entity instanceof Mob mob) {
						if (mob.checkSpawnRules(level, MobSpawnType.CHUNK_GENERATION) && mob.checkSpawnObstruction(level)) {
							var difficulty = level.getCurrentDifficultyAt(mob.blockPosition());

							//noinspection deprecation,OverrideOnly
							spawnGroupData = mob.finalizeSpawn(level, difficulty, MobSpawnType.CHUNK_GENERATION, spawnGroupData, null);
							mobSpawned = true;

							level.addFreshEntityWithPassengers(mob);
						}
					}
				}
			}
		}
	}

	@Nullable
	private static Entity trySpawnEntity(ServerLevel level, RandomSource random, MobSpawnSettings.SpawnerData spawnerData, BlockPos blockPos) {

		var placementType = SpawnPlacements.getPlacementType(spawnerData.type);
		var canSpawn = spawnerData.type.canSummon()
				&& NaturalSpawner.isSpawnPositionOk(placementType, level, blockPos, spawnerData.type);

		if (!canSpawn) {
			return null;
		}

		var x = blockPos.getX();
		var y = blockPos.getY();
		var z = blockPos.getZ();
		var aabb = spawnerData.type.getAABB(x, y, z);

		var hasCollisions = !level.noCollision(aabb);
		var placementSpawnRulesPassed = SpawnPlacements.checkSpawnRules(
				spawnerData.type, level, MobSpawnType.CHUNK_GENERATION, blockPos, random);

		if (hasCollisions || !placementSpawnRulesPassed) {
			return null;
		}

		Entity entity = createEntity(level, spawnerData);

		if (entity == null) {
			return null;
		}

		entity.moveTo(x, y, z, random.nextFloat() * 360, 0);

		return entity;
	}

	@Nullable
	private static Entity createEntity(ServerLevel level, MobSpawnSettings.SpawnerData spawnerData) {
		try {
			return spawnerData.type.create(level);
		} catch (Exception exception) {
			LOGGER.warn("Failed to create mob", exception);
			return null;
		}
	}

	@FunctionalInterface
	public interface SpawnPosSupplierFactory {

		SpawnPosSupplier create(ServerLevel level, EntityType<?> entityType, ChunkPos chunkPos);
	}
}
