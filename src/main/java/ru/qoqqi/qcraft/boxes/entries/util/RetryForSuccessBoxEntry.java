package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class RetryForSuccessBoxEntry implements IBoxEntry {
	
	private final IBoxEntry entry;
	
	private int retryLimit;
	
	public RetryForSuccessBoxEntry(int retryLimit, IBoxEntry entry) {
		this.entry = entry;
		this.retryLimit = retryLimit;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result;
		
		do {
			result = entry.unpack(level, player, server, blockPos, lootBox);
		} while (!result.isSuccessful() && --retryLimit > 0);
		
		return result;
	}
}
