package ru.qoqqi.qcraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

import ru.qoqqi.qcraft.containers.PuzzleBoxContainer;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityEvents {
	
	private static final Random random = new Random();
	
	@SubscribeEvent
	public static void fixPuzzleBoxDrops(final LivingDeathEvent event) {
		// If the player is killed while solving a puzzle,
		// the item in hand is dropped into the world.
		// Just clean his hand when he dies.
		
		if (event.getEntity() instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) event.getEntity();
			
			if (player.openContainer instanceof PuzzleBoxContainer) {
				player.inventory.setItemStack(ItemStack.EMPTY);
			}
		}
	}
}
