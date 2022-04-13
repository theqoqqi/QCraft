package ru.qoqqi.qcraft.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.LootBox;

public class LootBoxItem extends Item {
	
	private final LootBox lootBox;
	
	public LootBoxItem(Properties properties, LootBox lootBox) {
		super(properties);
		this.lootBox = lootBox;
	}
	
	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity player, @Nonnull Hand handIn) {
		
		ItemStack itemStack = player.getHeldItem(handIn);
		
		ActionResultType resultType = this.lootBox.openWithActionResult(player, itemStack, player.getPosition());
		
		if (resultType == ActionResultType.FAIL) {
			return ActionResult.resultFail(itemStack);
		}
		
		if (!player.abilities.isCreativeMode) {
			itemStack.shrink(1);
		}
		
		return ActionResult.resultConsume(itemStack);
	}
}
