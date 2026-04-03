package ru.qoqqi.qcraft.boxes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.entries.AttributeBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.ExperienceBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.ExplosionBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.ItemStackBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.LootTableBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.StructureBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.SummonBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.CompositeBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RandomBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RepeatBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RetryForSuccessBoxEntry;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.util.IntRange;
import ru.qoqqi.qcraft.util.WeightedList;

public class LootBoxes {

	private static final Map<String, LootBox> byNames = new HashMap<>();

	public static final LootBox LOOT_BOX_SMALL = create(
			"loot_box_small",
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_small")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(100, 20)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(1, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);

	public static final LootBox LOOT_BOX_MEDIUM = create(
			"loot_box_medium",
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_medium")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(200, 40)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(2, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);

	public static final LootBox LOOT_BOX_LARGE = create(
			"loot_box_large",
			new WeightedList.WeightedEntry<>(8, new LootTableBoxEntry("loot_box_large")),
			new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(300, 60)),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8)))
	);

	public static final LootBox NOAHS_BOX = create(
			"noahs_box",
			new WeightedList.WeightedEntry<>(4, new RepeatBoxEntry(1, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(3, new RepeatBoxEntry(2, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(2, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2))),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(5, new SummonBoxEntry(SummonBoxEntry.GROUND_CREATURES, 8, 2)))
	);

	public static final LootBox POSEIDONS_BOX = create(
			"poseidons_box",
			new WeightedList.WeightedEntry<>(4, new RepeatBoxEntry(3, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(3, new RepeatBoxEntry(5, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(2, new RepeatBoxEntry(7, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4))),
			new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(10, new SummonBoxEntry(SummonBoxEntry.WATER_CREATURES, 4)))
	);

	public static final LootBox ATTRIBUTE_BOX = create(
			"attribute_box",
			new RetryForSuccessBoxEntry(10, new RandomBoxEntry(AttributeBoxEntry.INCREASE_ATTRIBUTE_ENTRIES)),
			new RetryForSuccessBoxEntry(10, new RandomBoxEntry(AttributeBoxEntry.DECREASE_ATTRIBUTE_ENTRIES))
	);

	public static final LootBox PUZZLE_BOX_EASY = create(
			"puzzle_box_easy",
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_easy"))
	);

	public static final LootBox PUZZLE_BOX_NORMAL = create(
			"puzzle_box_normal",
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_normal").withProcessor(new LootTableBoxEntry.UpgradeEnchantmentsProcessor(0, 1)))
	);

	public static final LootBox PUZZLE_BOX_HARD = create(
			"puzzle_box_hard",
			new WeightedList.WeightedEntry<>(1, new LootTableBoxEntry("puzzle_box_hard").withProcessor(new LootTableBoxEntry.UpgradeEnchantmentsProcessor(1, 2)))
	);

	public static final LootBox PANDORAS_BOX = create(
			"pandroas_box",
			new CompositeBoxEntry(Arrays.asList(
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
			))
	);

	public static final LootBox TRAVELERS_HOME = create(
			"journey_travelers_home_reward",
			new CompositeBoxEntry(Arrays.asList(
					new ItemStackBoxEntry(JourneyStages.TRAVELERS_HOME::createNotes),
					new LootTableBoxEntry("journey/travelers_home/reward"),
					new ExperienceBoxEntry(100, 10)
			))
	);

	public static final LootBox FORTUNE_ISLAND = create(
			"journey_fortune_island_reward",
			new CompositeBoxEntry(Arrays.asList(
					new ItemStackBoxEntry(JourneyStages.FORTUNE_ISLAND::createNotes),
					new LootTableBoxEntry("journey/fortune_island/reward"),
					new ExperienceBoxEntry(200, 20)
			))
	);

	public static final LootBox JUNGLE_TEMPLE = create(
			"journey_jungle_temple_reward",
			new CompositeBoxEntry(Arrays.asList(
					new ItemStackBoxEntry(JourneyStages.JUNGLE_TEMPLE::createNotes),
					new LootTableBoxEntry("journey/jungle_temple/reward"),
					new ExperienceBoxEntry(300, 30)
			))
	);

	public static final LootBox MANGROVE_TEMPLE = create(
			"journey_mangrove_temple_reward",
			new CompositeBoxEntry(Arrays.asList(
					new ItemStackBoxEntry(JourneyStages.MANGROVE_TEMPLE::createNotes),
					new LootTableBoxEntry("journey/mangrove_temple/reward"),
					new ExperienceBoxEntry(400, 40)
			))
	);

	public static final LootBox PANDORAS_TEMPLE = create(
			"journey_jungle_temple_reward",
			new CompositeBoxEntry(Arrays.asList(
					new ItemStackBoxEntry(JourneyStages.PANDORAS_TEMPLE::createNotes),
					new LootTableBoxEntry("journey/pandoras_temple/reward"),
					new ExperienceBoxEntry(500, 50)
			))
	);

	public static final LootBox PLAINS_JELLY_BLOB_LOOT_BOX = create(
			"plains_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:chicken",
					"minecraft:cow",
					"minecraft:pig",
					"minecraft:sheep",
					"q_craft:field_mouse",
					"q_craft:stone_crab"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox FOREST_JELLY_BLOB_LOOT_BOX = create(
			"forest_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:chicken",
					"minecraft:cow",
					"minecraft:pig",
					"minecraft:sheep"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox DESERT_JELLY_BLOB_LOOT_BOX = create(
			"desert_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:husk",
					"minecraft:rabbit"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox BEACH_JELLY_BLOB_LOOT_BOX = create(
			"beach_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:turtle",
					"q_craft:stone_crab"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox SNOWY_JELLY_BLOB_LOOT_BOX = create(
			"snowy_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:polar_bear"
			), 2, 1))
	);

	public static final LootBox SWAMP_JELLY_BLOB_LOOT_BOX = create(
			"swamp_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:frog"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:slime"
			), 2, 1))
	);

	public static final LootBox JUNGLE_JELLY_BLOB_LOOT_BOX = create(
			"jungle_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:ocelot",
					"minecraft:panda",
					"minecraft:parrot"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox SAVANNA_JELLY_BLOB_LOOT_BOX = create(
			"savanna_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:cow",
					"minecraft:horse",
					"minecraft:llama",
					"minecraft:pig",
					"minecraft:sheep",
					"q_craft:field_mouse",
					"q_craft:stone_crab"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox BADLANDS_JELLY_BLOB_LOOT_BOX = create(
			"badlands_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:husk"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox HONEY_JELLY_BLOB_LOOT_BOX = create(
			"honey_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:bee"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox MUSHROOM_JELLY_BLOB_LOOT_BOX = create(
			"mushroom_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:mooshroom"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox RIVER_JELLY_BLOB_LOOT_BOX = create(
			"river_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:cod",
					"minecraft:salmon",
					"minecraft:pufferfish",
					"minecraft:turtle",
					"minecraft:squid",
					"minecraft:drowned"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox OCEAN_JELLY_BLOB_LOOT_BOX = create(
			"ocean_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:cod",
					"minecraft:dolphin",
					"minecraft:glow_squid",
					"minecraft:pufferfish",
					"minecraft:salmon",
					"minecraft:squid",
					"minecraft:tropical_fish",
					"minecraft:drowned",
					"minecraft:guardian"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:elder_guardian"
			), 2, 1))
	);

	public static final LootBox UNDERGROUND_JELLY_BLOB_LOOT_BOX = create(
			"underground_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:spider",
					"minecraft:zombie"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:creeper",
					"minecraft:slime",
					"minecraft:skeleton"
			), 2, 1))
	);

	public static final LootBox DEEP_UNDERGROUND_JELLY_BLOB_LOOT_BOX = create(
			"deep_underground_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(7, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:silverfish",
					"minecraft:skeleton",
					"minecraft:spider",
					"minecraft:zombie"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:cave_spider",
					"minecraft:creeper",
					"minecraft:slime",
					"minecraft:witch"
			), 2, 1))
	);

	public static final LootBox DRIPSTONE_CAVES_JELLY_BLOB_LOOT_BOX = create(
			"dripstone_caves_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:spider",
					"minecraft:zombie"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:creeper",
					"minecraft:slime",
					"minecraft:skeleton"
			), 2, 1))
	);

	public static final LootBox LUSH_CAVES_JELLY_BLOB_LOOT_BOX = create(
			"lush_caves_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:axolotl",
					"minecraft:spider",
					"minecraft:zombie"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:creeper",
					"minecraft:slime",
					"minecraft:skeleton"
			), 2, 1))
	);

	public static final LootBox DEEP_DARK_JELLY_BLOB_LOOT_BOX = create(
			"deep_dark_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:silverfish",
					"minecraft:skeleton",
					"minecraft:spider",
					"minecraft:zombie"
			), 2, IntRange.of(4, 5))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:cave_spider",
					"minecraft:creeper",
					"minecraft:slime",
					"minecraft:witch"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox NETHER_JELLY_BLOB_LOOT_BOX = create(
			"nether_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(8, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:blaze",
					"minecraft:piglin_brute",
					"minecraft:zombified_piglin"
			), 2, IntRange.of(2, 3))),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:ghast",
					"minecraft:magma_cube"
			), 2, 1))
	);

	public static final LootBox END_PEARLS_JELLY_BLOB_LOOT_BOX = create(
			"end_pearls_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:enderman"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox END_CHORUS_JELLY_BLOB_LOOT_BOX = create(
			"end_chorus_jelly_blob_loot_box",
			new WeightedList.WeightedEntry<>(4, IBoxEntry.EMPTY),
			new WeightedList.WeightedEntry<>(1, new SummonBoxEntry(WeightedList.create(
					"minecraft:enderman",
					"minecraft:endermite",
					"minecraft:shulker"
			), 2, IntRange.of(2, 3)))
	);

	public static final LootBox RAINBOW_JELLY_BLOB_LOOT_BOX = create(
			"rainbow_jelly_blob_loot_box",
			new CompositeBoxEntry(Arrays.asList(
					new RandomBoxEntry(WeightedList.create(
							new WeightedList.WeightedEntry<>(6, new RepeatBoxEntry(3, new LootTableBoxEntry("random_loot_box"))),
							new WeightedList.WeightedEntry<>(3, new RepeatBoxEntry(4, new LootTableBoxEntry("random_loot_box"))),
							new WeightedList.WeightedEntry<>(1, new RepeatBoxEntry(5, new LootTableBoxEntry("random_loot_box")))
					)),
					new RandomBoxEntry(WeightedList.create(
							new WeightedList.WeightedEntry<>(6, new ExperienceBoxEntry(300, 30)),
							new WeightedList.WeightedEntry<>(3, new ExperienceBoxEntry(400, 40)),
							new WeightedList.WeightedEntry<>(1, new ExperienceBoxEntry(500, 50))
					))
			))
	);

	@SafeVarargs
	private static LootBox create(String name, WeightedList.WeightedEntry<IBoxEntry>... entries) {
		return register(name, WeightedList.create(entries));
	}

	private static LootBox create(String name, IBoxEntry... entries) {
		return register(name, WeightedList.create(entries));
	}

	private static LootBox register(String name, WeightedList<IBoxEntry> entries) {
		var lootBox = new LootBox(name, entries);

		byNames.put(name, lootBox);

		return lootBox;
	}

	public static LootBox byName(String name) {
		return byNames.get(name);
	}
}
