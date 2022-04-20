package ru.qoqqi.qcraft.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.criterion.PositionTrigger;
import net.minecraft.util.ResourceLocation;

import ru.qoqqi.qcraft.QCraft;

public class ModCriteriaTriggers {
	
	public static final LootBoxTrigger OPEN_LOOT_BOX = register(new LootBoxTrigger());
	
	public static final SolvePuzzleTrigger SOLVE_PUZZLE = register(new SolvePuzzleTrigger());
	
	public static final PositionTrigger OPEN_PANDORAS_BOX = register(new PositionTrigger(new ResourceLocation(QCraft.MOD_ID, "open_pandoras_box")));
	
	public static final PositionTrigger ACTIVATE_LOOT_BOX_GENERATOR = register(new PositionTrigger(new ResourceLocation(QCraft.MOD_ID, "activate_loot_box_generator")));
	
	public static <T extends ICriterionTrigger<?>> T register(T criterion) {
		return CriteriaTriggers.register(criterion);
	}
	
	public static void register() {
		// Used to init statics
	}
}
