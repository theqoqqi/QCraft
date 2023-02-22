package ru.qoqqi.qcraft;

import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.qoqqi.qcraft.containers.PuzzleBoxMenu;
import ru.qoqqi.qcraft.entities.FieldMouse;
import ru.qoqqi.qcraft.entities.ai.CustomNonTameRandomTargetGoal;

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
	
	@SubscribeEvent
	public static void addMouseTargetGoalToCats(final EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof Cat cat)) {
			return;
		}
		
		if (cat.targetSelector.getAvailableGoals().stream().noneMatch(EntityEvents::isMouseTargetGoal)) {
			cat.targetSelector.addGoal(1, new CustomNonTameRandomTargetGoal<>(cat, FieldMouse.class, false, null));
		}
	}
	
	private static boolean isMouseTargetGoal(WrappedGoal wrappedGoal) {
		var goal = wrappedGoal.getGoal();
		
		if (!(goal instanceof CustomNonTameRandomTargetGoal<?> targetGoal)) {
			return false;
		}
		
		return targetGoal.getTargetType() == FieldMouse.class;
	}
}
