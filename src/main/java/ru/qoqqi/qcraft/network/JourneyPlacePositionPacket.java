package ru.qoqqi.qcraft.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.function.Supplier;

import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;

public class JourneyPlacePositionPacket {

	private final JourneyStage stage;

	private final BlockPos position;

	public JourneyPlacePositionPacket(JourneyStage stage, BlockPos position) {
		this.stage = stage;
		this.position = position;
	}

	public JourneyPlacePositionPacket(FriendlyByteBuf buf) {
		this.stage = JourneyStages.byName(buf.readUtf());
		this.position = buf.readBlockPos();
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(stage.name);
		buf.writeBlockPos(position);
	}

	public static class Handler {

		public static void onMessage(JourneyPlacePositionPacket packet, CustomPayloadEvent.Context ctx) {
			ctx.enqueueWork(() -> {
				JourneyLevelData.Client.setPlacePosition(packet.stage, packet.position);
			});

			ctx.setPacketHandled(true);
		}
	}
}
