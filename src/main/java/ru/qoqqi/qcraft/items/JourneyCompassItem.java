package ru.qoqqi.qcraft.items;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import ru.qoqqi.qcraft.leveldata.JourneyLevelData;

public class JourneyCompassItem extends Item {

	private static final int NEARBY_DISTANCE = 128;

	public JourneyCompassItem(Properties properties) {
		super(properties);
	}

	public static GlobalPos getNextPlacePosition(LocalPlayer player) {
		if (player.level().dimensionTypeId() != BuiltinDimensionTypes.OVERWORLD) {
			return null;
		}

		ResourceKey<Level> dimension = player.level().dimension();
		BlockPos blockPos = JourneyLevelData.Client.getNextPlacePosition();

		if (blockPos == null || isNearby(blockPos, player)) {
			return null;
		}

		return GlobalPos.of(dimension, blockPos);
	}

	private static boolean isNearby(BlockPos blockPos, LocalPlayer player) {
		return blockPos.closerToCenterThan(player.getPosition(0), NEARBY_DISTANCE);
	}
}
