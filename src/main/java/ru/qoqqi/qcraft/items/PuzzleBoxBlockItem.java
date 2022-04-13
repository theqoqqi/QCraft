package ru.qoqqi.qcraft.items;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PuzzleBoxBlockItem extends BlockItem {
	
	public PuzzleBoxBlockItem(Block blockIn, Properties builder) {
		super(blockIn, builder);
	}
	
	public long getSeed(@Nonnull ItemStack stack, @Nullable World world) {
		
		if (!hasSeed(stack, world)) {
			return 0;
		}
		
		CompoundNBT blockEntityTag = stack.getOrCreateChildTag("BlockEntityTag");
		
		if (!blockEntityTag.contains("seed")) {
			blockEntityTag.putLong("seed", world.rand.nextLong());
		}
		
		return blockEntityTag.getLong("seed");
	}
	
	private boolean hasSeed(@Nonnull ItemStack stack, @Nullable World world) {
		return world != null && (!world.isRemote || stack.hasTag());
	}
	
	@Override
	public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
		super.addInformation(stack, world, tooltip, flag);
		
		if (hasSeed(stack, world) && flag.isAdvanced()) {
			tooltip.add(new TranslationTextComponent("puzzleBox.seed", getSeed(stack, world)));
		}
	}
}
