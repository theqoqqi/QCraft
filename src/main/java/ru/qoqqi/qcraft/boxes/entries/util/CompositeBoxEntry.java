package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nonnull;

public class CompositeBoxEntry implements IBoxEntry {
	
	protected final List<IBoxEntry> entries;
	
	public CompositeBoxEntry(List<IBoxEntry> entries) {
		this.entries = entries;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		
		entries.forEach(entry -> result.merge(entry.unpack(world, player, server, blockPos, lootBox)));
		
		return result;
	}
}
