package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		
		for (int i = 0; i < count; i++) {
			result.merge(entry.unpack(world, player, server, blockPos, lootBox));
		}
		
		return result;
	}
}
