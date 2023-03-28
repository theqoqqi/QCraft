package ru.qoqqi.qcraft.boxes;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemUtils {
	
	public static void giveOrDropItem(Player player, ItemStack itemStack) {
		boolean fullyAdded = player.getInventory().add(itemStack);
		
		if (fullyAdded && itemStack.isEmpty()) {
			itemStack.setCount(1);
			
			ItemEntity itemEntity = player.drop(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.makeFakeItem();
			}
			
			playPickupSound(player);
			player.containerMenu.broadcastChanges();
			
		} else {
			ItemEntity itemEntity = player.drop(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.setNoPickUpDelay();
				itemEntity.setOwner(player.getUUID());
			}
		}
	}
	
	private static void playPickupSound(Player player) {
		double posX = player.getX();
		double posY = player.getY();
		double posZ = player.getZ();
		SoundEvent sound = SoundEvents.ITEM_PICKUP;
		SoundSource category = SoundSource.PLAYERS;
		float volume = 0.2F;
		RandomSource random = player.getRandom();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.8F + 1.0F) * 2.0F;
		
		player.level.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
	}
}
