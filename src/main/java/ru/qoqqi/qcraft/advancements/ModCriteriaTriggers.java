package ru.qoqqi.qcraft.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.resources.ResourceLocation;

import ru.qoqqi.qcraft.QCraft;

public class ModCriteriaTriggers {

	public static final LootBoxTrigger OPEN_LOOT_BOX = register("open_loot_box", new LootBoxTrigger());

	public static final SolvePuzzleTrigger SOLVE_PUZZLE = register("solve_puzzle", new SolvePuzzleTrigger());

	public static final PlayerTrigger OPEN_PANDORAS_BOX = register("open_pandoras_box", new PlayerTrigger());

	public static final PlayerTrigger ACTIVATE_LOOT_BOX_GENERATOR = register("activate_loot_box_generator", new PlayerTrigger());

	public static <T extends CriterionTrigger<?>> T register(String id, T criterion) {
		return CriteriaTriggers.register(QCraft.MOD_ID + ":" + id, criterion);
	}

	public static void register() {
		// Used to init statics
	}
}
