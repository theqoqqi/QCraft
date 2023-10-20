package ru.qoqqi.qcraft.level.spawners;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelEvents {

	private static final Logger LOGGER = LogManager.getLogger();

	private static List<CustomSpawner> customSpawners;

	@SubscribeEvent
	public static void setupCustomSpawners(final LevelEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			return;
		}

		if (level.dimensionTypeId() != BuiltinDimensionTypes.OVERWORLD) {
			return;
		}

		customSpawners = List.of(new MouseSpawner());
		LOGGER.info("Registered {} custom spawners", customSpawners.size());
	}

	@SubscribeEvent
	public static void tickCustomSpawners(final TickEvent.LevelTickEvent event) {
		if (!(event.level instanceof ServerLevel level)) {
			return;
		}

		if (event.side != LogicalSide.SERVER || event.phase != TickEvent.Phase.START) {
			return;
		}

		if (!level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
			return;
		}

		var server = level.getServer();

		for (var customSpawner : customSpawners) {
			var spawnEnemies = server.isSpawningMonsters();
			var spawnFriendlies = server.isSpawningAnimals();

			customSpawner.tick(level, spawnEnemies, spawnFriendlies);
		}
	}
}
