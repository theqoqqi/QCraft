package ru.qoqqi.qcraft.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

import ru.qoqqi.qcraft.QCraft;

public class ModCriteriaTriggers {
	
	public static final LootBoxTrigger OPEN_LOOT_BOX = register(new LootBoxTrigger());
	
	public static final SolvePuzzleTrigger SOLVE_PUZZLE = register(new SolvePuzzleTrigger());
	
	public static final PlayerTrigger OPEN_PANDORAS_BOX = register(new PlayerTrigger(new ResourceLocation(QCraft.MOD_ID, "open_pandoras_box")));
	
	public static final PlayerTrigger ACTIVATE_LOOT_BOX_GENERATOR = register(new PlayerTrigger(new ResourceLocation(QCraft.MOD_ID, "activate_loot_box_generator")));
	
	public static <T extends CriterionTrigger<?>> T register(T criterion) {
		return CriteriaTriggers.register(criterion);
	}
	
	public static void register() {
		// Used to init statics
	}
}
