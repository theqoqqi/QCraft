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
	}
}
