package ru.qoqqi.qcraft.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blocks.ModBlocks;
import ru.qoqqi.qcraft.items.ModItems;

public class JourneyRewardBlockEntity extends BlockEntity implements ItemPedestal {
	
	private int age;
	
	public final float hoverStart;
	
	@Nonnull
	private final ItemStack itemStack;
	
	public JourneyRewardBlockEntity(BlockPos pos, BlockState blockState) {
		this(ModBlockEntityTypes.JOURNEY_REWARD.get(), pos, blockState);
	}
	
	public JourneyRewardBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
		super(blockEntityType, pos, blockState);
		hoverStart = (float) (Math.random() * Math.PI * 2.0);
		itemStack = getItemStackForBlock(blockState.getBlock());
	}
	
	private ItemStack getItemStackForBlock(Block block) {
		return new ItemStack(getItemForBlock(block));
	}
	
	private Item getItemForBlock(Block block) {
		if (block == ModBlocks.TRAVELERS_HOME_JOURNEY_REWARD_BLOCK.get()) {
			return ModItems.JOURNEY_COMPASS.get();
		}
		
		if (block == ModBlocks.FORTUNE_ISLAND_JOURNEY_REWARD_BLOCK.get()) {
			return ModItems.GIFT_BOX_LARGE.get();
		}
		
		if (block == ModBlocks.JUNGLE_TEMPLE_JOURNEY_REWARD_BLOCK.get()) {
			return ModItems.NOAHS_BOX.get();
		}
		
		if (block == ModBlocks.MANGROVE_TEMPLE_JOURNEY_REWARD_BLOCK.get()) {
			return ModBlocks.PUZZLE_BOX_HARD.get().asItem();
		}
		
		if (block == ModBlocks.PANDORAS_TEMPLE_JOURNEY_REWARD_BLOCK.get()) {
			return ModBlocks.PANDORAS_BOX.get().asItem();
		}
		
		return Items.AIR;
	}
	
	public void tick() {
		age++;
	}
	
	@Override
	public int getAge() {
		return age;
	}
	
	@Override
	public float getHoverStart() {
		return hoverStart;
	}
	
	@Override
	public Level getLevel2() {
		return getLevel();
	}
	
	@Override
	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	@Override
	public boolean hasItem() {
		return !itemStack.isEmpty();
	}
}
