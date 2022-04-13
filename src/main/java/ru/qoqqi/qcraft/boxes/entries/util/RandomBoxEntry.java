package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.util.RandomUtils;

public class RandomBoxEntry implements IBoxEntry {
	
	protected final WeightedList<IBoxEntry> entries;
	
	public RandomBoxEntry(IBoxEntry entityName) {
		this.entries = RandomUtils.createWeightedList(new RandomUtils.WeightedEntry<>(1, entityName));
	}
	
	public RandomBoxEntry(WeightedList<IBoxEntry> entries) {
		this.entries = entries;
	}
	
	public RandomBoxEntry(IBoxEntry... entries) {
		this(Arrays.asList(entries));
	}
	
	public RandomBoxEntry(List<? extends IBoxEntry> entries) {
		this.entries = new WeightedList<>();
		entries.forEach(entry -> this.entries.addWeighted(entry, 1));
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		IBoxEntry randomEntry = entries.getRandomValue(world.getRandom());
		
		return randomEntry.unpack(world, player, server, blockPos, lootBox);
	}
}
