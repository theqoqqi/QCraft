package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result;
		
		do {
			result = entry.unpack(world, player, server, blockPos, lootBox);
		} while (!result.isSuccessful() && --retryLimit > 0);
		
		return result;
	}
}
