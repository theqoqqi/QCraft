package ru.qoqqi.qcraft;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;
import ru.qoqqi.qcraft.network.JourneyPlaceVisitedPacket;
import ru.qoqqi.qcraft.network.JourneyPlacePositionPacket;
import ru.qoqqi.qcraft.network.ModPacketHandler;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLoadLevel(final PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
			return;
		}
		
		ServerLevel level = serverPlayer.getLevel();
		
		if (level.dimensionTypeId() == BuiltinDimensionTypes.OVERWORLD) {
			JourneyLevelData.setLoadingInstance(level);
			JourneyLevelData levelData = JourneyLevelData.getInstance(level);
			Map<JourneyStage, BlockPos> placePositions = levelData.getPlacePositions();
			
			placePositions.forEach((stage, position) -> {
				JourneyPlacePositionPacket packet = new JourneyPlacePositionPacket(stage, position);
				
				ModPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), packet);
				
				if (levelData.isVisitedBy(stage, serverPlayer.getUUID())) {
					JourneyPlaceVisitedPacket placeVisitedPacket = new JourneyPlaceVisitedPacket(stage);
					
					ModPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), placeVisitedPacket);
				}
			});
		}
	}
}
