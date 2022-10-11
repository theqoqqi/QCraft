package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.util.WeightedList;

public class RandomBoxEntry implements IBoxEntry {
	
	protected final WeightedList<IBoxEntry> entries;
	
	public RandomBoxEntry(IBoxEntry entityName) {
		this.entries = WeightedList.create(new WeightedList.WeightedEntry<>(1, entityName));
	}
	
	public RandomBoxEntry(WeightedList<IBoxEntry> entries) {
		this.entries = entries;
	}
	
	public RandomBoxEntry(IBoxEntry... entries) {
		this(Arrays.asList(entries));
	}
	
	public RandomBoxEntry(List<? extends IBoxEntry> entries) {
		this.entries = WeightedList.create(entries);
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		IBoxEntry randomEntry = entries.getRandomValue(level.getRandom());
		
		return randomEntry.unpack(level, player, server, blockPos, lootBox);
	}
}
