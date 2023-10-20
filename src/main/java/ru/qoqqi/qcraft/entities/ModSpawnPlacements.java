package ru.qoqqi.qcraft.entities;

import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

public class ModSpawnPlacements {

	public static void register() {
		SpawnPlacements.register(
				ModEntityTypes.STONE_CRAB.get(),
				SpawnPlacements.Type.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				StoneCrab::checkStoneCrabSpawnRules
		);

		SpawnPlacements.register(
				ModEntityTypes.FIELD_MOUSE.get(),
				SpawnPlacements.Type.ON_GROUND,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				FieldMouse::checkFieldMouseSpawnRules
		);

		SpawnPlacements.register(
				ModEntityTypes.JELLY_BLOB.get(),
				SpawnPlacements.Type.NO_RESTRICTIONS,
				Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				JellyBlob::checkJellyBlobSpawnRules
		);
	}
}
