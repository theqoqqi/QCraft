package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

import javax.annotation.Nonnull;

public class CompositeBoxEntry implements IBoxEntry {

	protected final List<IBoxEntry> entries;

	public CompositeBoxEntry(List<IBoxEntry> entries) {
		this.entries = entries;
	}

	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);

		entries.forEach(entry -> result.merge(entry.unpack(level, player, server, blockPos, lootBox)));

		return result;
	}
}
