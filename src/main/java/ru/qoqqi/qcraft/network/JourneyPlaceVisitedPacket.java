package ru.qoqqi.qcraft.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;

public class JourneyPlaceVisitedPacket {

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

		public static void onMessage(JourneyPlaceVisitedPacket packet, CustomPayloadEvent.Context ctx) {
			ctx.enqueueWork(() -> {
				JourneyLevelData.Client.setPlaceVisited(packet.stage);
			});

			ctx.setPacketHandled(true);
		}
	}
}
