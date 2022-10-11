package ru.qoqqi.qcraft.boxes;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;

import java.util.Arrays;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.entries.AttributeBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.ExperienceBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.ExplosionBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.LootTableBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.StructureBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.SummonBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.CompositeBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RandomBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RepeatBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RetryForSuccessBoxEntry;
import ru.qoqqi.qcraft.util.WeightedList;

public class LootBoxes {
	
	public static final LootBox LOOT_BOX_SMALL = create(
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_small")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(100, 20)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(1, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);
	
	public static final LootBox LOOT_BOX_MEDIUM = create(
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_medium")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(200, 40)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(2, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);
	
	public static final LootBox LOOT_BOX_LARGE = create(
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_large")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(300, 60)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);
	
	public static final LootBox NOAHS_BOX = create(
			new WeightedList.WeightedEntry<>(4, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(3, new RepeatBoxEntry(5, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(2, new RepeatBoxEntry(7, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(10, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2)))
	);
	
	public static final LootBox POSEIDONS_BOX = create(
			new WeightedList.WeightedEntry<>(4, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(3, new RepeatBoxEntry(5, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(2, new RepeatBoxEntry(7, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(10, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4)))
	);
	
	public static final LootBox ATTRIBUTE_BOX = create(
			new RetryForSuccessBoxEntry(10, new RandomBoxEntry(AttributeBoxEntry.INCREASE_ATTRIBUTE_ENTRIES)),
			new RetryForSuccessBoxEntry(10, new RandomBoxEntry(AttributeBoxEntry.DECREASE_ATTRIBUTE_ENTRIES))
	);
	
	public static final LootBox PUZZLE_BOX_EASY = create(
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_easy"))
	);
	
	public static final LootBox PUZZLE_BOX_NORMAL = create(
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_normal").withProcessor(new LootTableBoxEntry.UpgradeEnchantmentsProcessor(0, 1)))
	);
	
	public static final LootBox PUZZLE_BOX_HARD = create(
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_hard").withProcessor(new LootTableBoxEntry.UpgradeEnchantmentsProcessor(1, 2)))
	);
	
	public static final LootBox PANDORAS_BOX = create(new CompositeBoxEntry(Arrays.asList(
			new RandomBoxEntry(WeightedList.create(
					new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(5000, 1000)),
					new WeightedList.WeightedEntry<>(1, new StructureBoxEntry(new ResourceLocation(QCraft.MOD_ID, "end_portal"), new BlockPos(-2, 0, -2))),
					new WeightedList.WeightedEntry<>(6, new LootTableBoxEntry("pandoras_box").withProcessor(new LootTableBoxEntry.UpgradeEnchantmentsProcessor(3, 7)))
			)),
			new RandomBoxEntry(WeightedList.create(
					new WeightedList.WeightedEntry<>(3, new ExplosionBoxEntry(12)),
					new WeightedList.WeightedEntry<>(1, new SummonBoxEntry("minecraft:wither", 4)),
					new WeightedList.WeightedEntry<>(6, new RepeatBoxEntry(12, new SummonBoxEntry(SummonBoxEntry.MONSTERS, 12)))
			))
	)));
	
	@SafeVarargs
	private static LootBox create(WeightedList.WeightedEntry<IBoxEntry>... entries) {
		return new LootBox(WeightedList.create(entries));
	}
	
	private static LootBox create(IBoxEntry... entries) {
		return new LootBox(WeightedList.create(entries));
	}
}
