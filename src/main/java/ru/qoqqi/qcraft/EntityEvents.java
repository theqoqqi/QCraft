package ru.qoqqi.qcraft;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.qoqqi.qcraft.containers.PuzzleBoxMenu;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvents {
	
	@SubscribeEvent
	public static void fixPuzzleBoxDrops(final LivingDeathEvent event) {
		// If the player is killed while solving a puzzle,
		// the item in hand is dropped into the world.
		// Just clean his hand when he dies.
		
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}
		
		if (player.containerMenu instanceof PuzzleBoxMenu puzzleBoxMenu) {
			puzzleBoxMenu.setCarried(ItemStack.EMPTY);
		}
	}
}
