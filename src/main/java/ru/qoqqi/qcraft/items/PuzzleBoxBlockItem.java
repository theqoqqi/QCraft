package ru.qoqqi.qcraft.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleBoxBlockItem extends BlockItem {
	
	public PuzzleBoxBlockItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	public long getOrCreateSeed(@Nonnull ItemStack stack, @Nullable Level level) {
		if (!hasSeed(stack, level)) {
			return newSeed(level);
		}
		
		return getSeed(stack, level);
	}
	
	public long getSeed(@Nonnull ItemStack stack, @Nullable Level level) {
		if (!hasSeed(stack, level)) {
			return 0;
		}
		
		CompoundTag blockEntityTag = stack.getOrCreateTagElement("BlockEntityTag");
		
		return blockEntityTag.getLong("seed");
	}
	
	public void setSeed(@Nonnull ItemStack stack, long seed) {
		CompoundTag blockEntityTag = stack.getOrCreateTagElement("BlockEntityTag");
		
		blockEntityTag.putLong("seed", seed);
	}
	
	private boolean hasSeed(@Nonnull ItemStack stack, @Nullable Level level) {
		return level != null && stack.hasTag();
	}
	
	public long newSeed(@Nullable Level level) {
		return level == null ? 0 : level.random.nextLong();
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		
		if (hasSeed(stack, level) && flag.isAdvanced()) {
			tooltip.add(Component.translatable("puzzleBox.seed", getSeed(stack, level)));
		}
	}
}
