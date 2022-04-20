package ru.qoqqi.qcraft.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.boxes.LootBox;

public class LootBoxBlock extends Block {
	
	private final LootBox lootBox;
	
	public LootBoxBlock(Properties properties, LootBox lootBox) {
		super(properties);
		this.lootBox = lootBox;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	@Override
	public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
		if (world.isRemote) {
			return ActionResultType.SUCCESS;
		} else {
			ItemStack itemStack = new ItemStack(asItem());
			
			world.destroyBlock(pos, false);
			
			ActionResultType result = this.lootBox.openWithActionResult(player, itemStack, pos);
			
			ModCriteriaTriggers.OPEN_PANDORAS_BOX.trigger((ServerPlayerEntity) player);
			
			return result;
		}
	}
}
