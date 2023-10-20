package ru.qoqqi.qcraft.items;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.boxes.LootBox;

public class LootBoxItem extends Item {
	
	private final LootBox lootBox;
	
	public LootBoxItem(Properties properties, LootBox lootBox) {
		super(properties);
		this.lootBox = lootBox;
	}
	
	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player, @Nonnull InteractionHand handIn) {
		
		ItemStack itemStack = player.getItemInHand(handIn);
		
		InteractionResult resultType = this.lootBox.openWithActionResult(player, itemStack, player.blockPosition());
		
		if (resultType == InteractionResult.FAIL) {
			return InteractionResultHolder.fail(itemStack);
		}
		
		if (player instanceof ServerPlayer) {
			ModCriteriaTriggers.OPEN_LOOT_BOX.trigger((ServerPlayer) player, itemStack);
		}
		
		if (!player.isCreative()) {
			itemStack.shrink(1);
		}
		
		return InteractionResultHolder.consume(itemStack);
	}
}
