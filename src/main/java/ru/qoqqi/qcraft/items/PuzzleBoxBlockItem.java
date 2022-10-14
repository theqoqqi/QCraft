package ru.qoqqi.qcraft.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
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
		if (!hasSeed(stack)) {
			return newSeed(level);
		}
		
		return doGetSeed(stack);
	}
	
	public long getSeed(@Nonnull ItemStack stack) {
		if (!hasSeed(stack)) {
			return 0;
		}
		
		return doGetSeed(stack);
	}
	
	private long doGetSeed(@Nonnull ItemStack stack) {
		CompoundTag blockEntityTag = stack.getOrCreateTagElement("BlockEntityTag");
		
		return blockEntityTag.getLong("seed");
	}
	
	private boolean hasSeed(@Nonnull ItemStack stack) {
		return stack.hasTag() && doGetSeed(stack) != 0;
	}
	
	public static long newSeed(@Nullable Level level) {
		if (level == null) {
			return 0;
		}
		
		return generateSeed(level.random);
	}
	
	private static long generateSeed(RandomSource random) {
		long seed;
		
		do {
			seed = random.nextLong();
		} while (seed == 0);
		
		return seed;
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		
		if (hasSeed(stack) && flag.isAdvanced()) {
			tooltip.add(Component.translatable("puzzleBox.seed", getSeed(stack)));
		}
	}
}
