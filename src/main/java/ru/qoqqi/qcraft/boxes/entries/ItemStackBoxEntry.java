package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.boxes.ItemUtils;

public class ItemStackBoxEntry implements IBoxEntry {
	
	protected final Supplier<ItemStack> itemStackSupplier;
	
	public ItemStackBoxEntry(Supplier<ItemStack> itemStackSupplier) {
		this.itemStackSupplier = itemStackSupplier;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		ItemUtils.giveOrDropItem(player, itemStackSupplier.get());
		return UnpackResult.resultSuccess(lootBox, player);
	}
}
