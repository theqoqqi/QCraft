package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class RepeatBoxEntry implements IBoxEntry {

	private final IBoxEntry entry;

	private final int count;

	public RepeatBoxEntry(int count, IBoxEntry entry) {
		this.entry = entry;
		this.count = count;
	}

	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);

		for (int i = 0; i < count; i++) {
			result.merge(entry.unpack(level, player, server, blockPos, lootBox));
		}

		return result;
	}
}
