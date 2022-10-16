package ru.qoqqi.qcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;

public class JourneyPlaceVisitedPacket {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final JourneyStage stage;
	
	public JourneyPlaceVisitedPacket(JourneyStage stage) {
		this.stage = stage;
	}
	
	public JourneyPlaceVisitedPacket(FriendlyByteBuf buf) {
		this.stage = JourneyStages.byName(buf.readUtf());
	}
	
	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(stage.name);
	}
	
	public static class Handler {
		
		public static void onMessage(JourneyPlaceVisitedPacket packet, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				JourneyLevelData.Client.setPlaceVisited(packet.stage);
			});
			
			ctx.get().setPacketHandled(true);
		}
	}
}
