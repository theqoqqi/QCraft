package ru.qoqqi.qcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

import ru.qoqqi.qcraft.QCraft;

public class ModPacketHandler {

	private static final String PROTOCOL_VERSION = "1";

	private static final ResourceLocation NAME = new ResourceLocation(QCraft.MOD_ID, "channel");

	public static final SimpleChannel CHANNEL = ChannelBuilder.named(NAME)
			.networkProtocolVersion(1)
			.simpleChannel()

			.messageBuilder(JourneyPlacePositionPacket.class)
			.decoder(JourneyPlacePositionPacket::new)
			.encoder(JourneyPlacePositionPacket::encode)
			.consumerNetworkThread(JourneyPlacePositionPacket.Handler::onMessage)
			.add()

			.messageBuilder(JourneyPlaceVisitedPacket.class)
			.decoder(JourneyPlaceVisitedPacket::new)
			.encoder(JourneyPlaceVisitedPacket::encode)
			.consumerNetworkThread(JourneyPlaceVisitedPacket.Handler::onMessage)
			.add();

	public static void init() {
		// Used to init statics
	}
}
