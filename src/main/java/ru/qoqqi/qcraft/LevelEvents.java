package ru.qoqqi.qcraft;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.qoqqi.qcraft.leveldata.JourneyLevelData;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelEvents {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void onLoadLevel(final LevelEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level)) {
			if (event.getLevel() instanceof ClientLevel) {
				JourneyLevelData.Client.init();
			}
			return;
		}
		
		if (level.dimensionTypeId() == BuiltinDimensionTypes.OVERWORLD) {
			JourneyLevelData.setLoadingInstance(level);
			
			LOGGER.info("LOADED PLACES:");
			JourneyLevelData.getInstance(level).getPlacePositions().forEach(((stage, blockPos) -> {
				LOGGER.info("PLACE {}: {}", stage.name, blockPos);
			}));
		}
	}
}
