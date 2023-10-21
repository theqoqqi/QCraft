package ru.qoqqi.qcraft.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.boxes.LootBoxes;
import ru.qoqqi.qcraft.items.ModItems;
import ru.qoqqi.qcraft.items.PuzzleBoxBlockItem;
import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.puzzles.PuzzleType;
import ru.qoqqi.qcraft.puzzles.PuzzleTypes;

public class ModBlocks {

	public static final DeferredRegister<Block> BLOCKS
			= DeferredRegister.create(ForgeRegistries.BLOCKS, QCraft.MOD_ID);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> OAK_PLATE = registerWoodenPlate("oak_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> BIRCH_PLATE = registerWoodenPlate("birch_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> ACACIA_PLATE = registerWoodenPlate("acacia_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> JUNGLE_PLATE = registerWoodenPlate("jungle_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> SPRUCE_PLATE = registerWoodenPlate("spruce_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> DARK_OAK_PLATE = registerWoodenPlate("dark_oak_plate");

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> MANGROVE_PLATE = registerWoodenPlate("mangrove_plate");

	public static final RegistryObject<Block> PUZZLE_BOX_EASY = registerPuzzleLootBox("puzzle_box_easy", 2, LootBoxes.PUZZLE_BOX_EASY, PuzzleTypes.PUZZLE_EASY);

	public static final RegistryObject<Block> PUZZLE_BOX_NORMAL = registerPuzzleLootBox("puzzle_box_normal", 4, LootBoxes.PUZZLE_BOX_NORMAL, PuzzleTypes.PUZZLE_NORMAL);

	public static final RegistryObject<Block> PUZZLE_BOX_HARD = registerPuzzleLootBox("puzzle_box_hard", 6, LootBoxes.PUZZLE_BOX_HARD, PuzzleTypes.PUZZLE_HARD);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PANDORAS_BOX = registerLootBox("pandoras_box", LootBoxes.PANDORAS_BOX);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> LOOT_BOX_GENERATOR_BLOCK = register(
			"loot_box_generator",
			() -> new LootBoxGeneratorBlock(
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.STONE)
							.strength(5.0f, 1200f)
			)
	);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> TRAVELERS_HOME_JOURNEY_REWARD_BLOCK =
			registerJourneyReward("travelers_home_journey_reward", JourneyStages.TRAVELERS_HOME, LootBoxes.TRAVELERS_HOME, MapColor.WOOD);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> FORTUNE_ISLAND_JOURNEY_REWARD_BLOCK =
			registerJourneyReward("fortune_island_journey_reward", JourneyStages.FORTUNE_ISLAND, LootBoxes.FORTUNE_ISLAND, MapColor.STONE);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> JUNGLE_TEMPLE_JOURNEY_REWARD_BLOCK =
			registerJourneyReward("jungle_temple_journey_reward", JourneyStages.JUNGLE_TEMPLE, LootBoxes.JUNGLE_TEMPLE, MapColor.WOOD);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> MANGROVE_TEMPLE_JOURNEY_REWARD_BLOCK =
			registerJourneyReward("mangrove_temple_journey_reward", JourneyStages.MANGROVE_TEMPLE, LootBoxes.MANGROVE_TEMPLE, MapColor.WOOD);

	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PANDORAS_TEMPLE_JOURNEY_REWARD_BLOCK =
			registerJourneyReward("pandoras_temple_journey_reward", JourneyStages.PANDORAS_TEMPLE, LootBoxes.PANDORAS_TEMPLE, MapColor.STONE);

	@SuppressWarnings("unused")
	private static RegistryObject<Block> registerWoodenPlate(String name) {
		return register(
				name,
				() -> new PlateBlock(
						BlockBehaviour.Properties.of()
								.strength(0.6f, 0.9f)
								.sound(SoundType.WOOD)
								.mapColor(MapColor.WOOD)
								.ignitedByLava()
								.noOcclusion()
				)
		);
	}

	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<Block> registerLootBox(String name, LootBox lootBox) {
		return register(
				name,
				() -> new LootBoxBlock(
						BlockBehaviour.Properties.of()
								.strength(1.0f, 5.0f)
								.sound(SoundType.WOOD)
								.mapColor(MapColor.WOOD)
								.ignitedByLava(),
						lootBox
				),
				new Item.Properties()
						.rarity(Rarity.EPIC)
		);
	}

	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<Block> registerJourneyReward(String name, JourneyStage stage, LootBox lootBox, MapColor mapColor) {
		BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
				.strength(25.0f, 1200f)
				.mapColor(mapColor);

		return register(name, () -> new JourneyRewardBlock(properties, stage, lootBox));
	}

	private static RegistryObject<Block> registerPuzzleLootBox(String name, int explosionPower, LootBox lootBox, PuzzleType config) {
		return register(
				name,
				() -> new PuzzleBoxBlock(
						BlockBehaviour.Properties.of()
								.strength(1.0f, 5.0f)
								.sound(SoundType.WOOD)
								.mapColor(MapColor.WOOD),
						lootBox,
						explosionPower,
						config
				),
				block -> new PuzzleBoxBlockItem(
						block,
						new Item.Properties().rarity(Rarity.EPIC)
				),
				true
		);
	}

	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier) {

		return register(name, blockSupplier, new Item.Properties());
	}

	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier, Item.Properties blockItemProperties) {

		return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties), true);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier,
			Function<T, ? extends BlockItem> blockItemFactory, boolean hasItemBlock) {

		final String actualName = name.toLowerCase(Locale.ROOT);
		final RegistryObject<T> block = BLOCKS.register(actualName, blockSupplier);

		if (hasItemBlock) {
			ModItems.ITEMS.register(actualName, () -> blockItemFactory.apply(block.get()));
		}

		return block;
	}
}
