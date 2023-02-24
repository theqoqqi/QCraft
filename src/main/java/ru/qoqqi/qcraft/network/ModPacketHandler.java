package ru.qoqqi.qcraft.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import ru.qoqqi.qcraft.QCraft;

public class ModPacketHandler {
	
	private static final String PROTOCOL_VERSION = "1";
	
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(QCraft.MOD_ID, "channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);
	
	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, JourneyPlacePositionPacket.class, JourneyPlacePositionPacket::encode, JourneyPlacePositionPacket::new, JourneyPlacePositionPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, JourneyPlaceVisitedPacket.class, JourneyPlaceVisitedPacket::encode, JourneyPlaceVisitedPacket::new, JourneyPlaceVisitedPacket.Handler::onMessage);
	}
}
