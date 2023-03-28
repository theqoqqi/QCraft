package ru.qoqqi.qcraft.level.spawners;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import ru.qoqqi.qcraft.entities.FieldMouse;
import ru.qoqqi.qcraft.entities.ModEntityTypes;

public class MouseSpawner implements CustomSpawner {
	
	private static final int TICK_DELAY = 600;
	
	private static final int MAX_MOUSES_IN_AREA = 3;
	
	private int nextTick;
	
	private final Predicate<Holder<PoiType>> mousePoiPredicate =
			poiTypeHolder -> poiTypeHolder.is(PoiTypes.FARMER);
	
	public int tick(@NotNull ServerLevel level, boolean spawnHostiles, boolean spawnPassives) {
		if (level.dimensionTypeId() != BuiltinDimensionTypes.OVERWORLD) {
			return 0;
		}
		
		if (!spawnPassives || !level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
			return 0;
		}
		
		--nextTick;
		
		if (nextTick > 0) {
			return 0;
		}
		
		nextTick = TICK_DELAY;
		
		var player = level.getRandomPlayer();
		
		if (player == null) {
			return 0;
		}
		
		var random = level.random;
		var xOffset = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
		var yOffset = (8 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
		
		var blockPos = player.blockPosition().offset(xOffset, 0, yOffset);
		
		if (!hasChunks(level, blockPos)) {
			return 0;
		}
		
		var spawnBlockPos = findSpawnPositionNear(level, blockPos);
		
		if (spawnBlockPos == null) {
			return 0;
		}
		
		if (level.isCloseToVillage(spawnBlockPos, 2)) {
			return spawnInVillage(level, spawnBlockPos);
		}
		
		if (isCloseToFarmland(level, spawnBlockPos)) {
			return spawnNearFarmland(level, spawnBlockPos);
		}
		
		return 0;
	}
	
	private static boolean hasChunks(@NotNull ServerLevel level, BlockPos blockPos) {
		var chunkCheckRange = 10;
		
		var checkFromX = blockPos.getX() - chunkCheckRange;
		var checkFromZ = blockPos.getZ() - chunkCheckRange;
		var checkToX = blockPos.getX() + chunkCheckRange;
		var checkToZ = blockPos.getZ() + chunkCheckRange;
		
		//noinspection deprecation
		return level.hasChunksAt(checkFromX, checkFromZ, checkToX, checkToZ);
	}
	
	private boolean isCloseToFarmland(ServerLevel level, BlockPos spawnBlockPos) {
		var searchArea = new AABB(spawnBlockPos).inflate(4, 1, 4);
		var blockStates = level.getBlockStates(searchArea);
		
		return blockStates.anyMatch(blockState -> blockState.is(Blocks.FARMLAND));
	}
	
	@Nullable
	private BlockPos findSpawnPositionNear(ServerLevel level, BlockPos blockPos) {
		var maxDistance = 4;
		
		for (var i = 0; i < 10; ++i) {
			var x = blockPos.getX() + level.random.nextInt(maxDistance * 2) - maxDistance;
			var z = blockPos.getZ() + level.random.nextInt(maxDistance * 2) - maxDistance;
			var y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
			var spawnBlockPos = new BlockPos(x, y, z);
			
			var spawnPositionOk = NaturalSpawner.isSpawnPositionOk(
					SpawnPlacements.Type.ON_GROUND,
					level,
					spawnBlockPos,
					ModEntityTypes.FIELD_MOUSE.get()
			);
			
			if (spawnPositionOk) {
				return spawnBlockPos;
			}
		}
		
		return null;
	}
	
	private int spawnInVillage(ServerLevel serverLevel, BlockPos blockPos) {
		int checkArea = 32;
		
		long farmsInRange = serverLevel.getPoiManager()
				.getCountInRange(mousePoiPredicate, blockPos, checkArea, PoiManager.Occupancy.ANY);
		
		if (farmsInRange < 1) {
			return 0;
		}
		
		return spawnMouse(serverLevel, blockPos, 32);
	}
	
	private int spawnNearFarmland(ServerLevel serverLevel, BlockPos blockPos) {
		return spawnMouse(serverLevel, blockPos, 24);
	}
	
	private int spawnMouse(ServerLevel serverLevel, BlockPos blockPos, int checkArea) {
		var aabb = new AABB(blockPos).inflate(checkArea, 8.0D, checkArea);
		var mousesInRange = serverLevel.getEntitiesOfClass(FieldMouse.class, aabb);
		
		if (mousesInRange.size() >= MAX_MOUSES_IN_AREA) {
			return 0;
		}
		
		var mouse = ModEntityTypes.FIELD_MOUSE.get().create(serverLevel);
		
		if (mouse == null) {
			return 0;
		}
		
		mouse.moveTo(blockPos, 0.0F, 0.0F);
		
		if (ForgeHooks.canEntitySpawn(mouse, serverLevel, blockPos.getX(), blockPos.getY(), blockPos.getZ(), null, MobSpawnType.NATURAL) == -1) {
			return 0;
		}
		
		var difficulty = serverLevel.getCurrentDifficultyAt(blockPos);
		
		mouse.finalizeSpawn(serverLevel, difficulty, MobSpawnType.NATURAL, null, null);
		serverLevel.addFreshEntityWithPassengers(mouse);
		
		return 1;
	}
}
