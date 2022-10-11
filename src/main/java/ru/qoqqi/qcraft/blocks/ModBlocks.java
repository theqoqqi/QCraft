package ru.qoqqi.qcraft.blocks;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.boxes.LootBoxes;
import ru.qoqqi.qcraft.items.ModItems;
import ru.qoqqi.qcraft.items.PuzzleBoxBlockItem;
import ru.qoqqi.qcraft.puzzles.PuzzleType;
import ru.qoqqi.qcraft.puzzles.PuzzleTypes;

public class ModBlocks {
	
	public static final DeferredRegister<Block> BLOCKS
			= DeferredRegister.create(ForgeRegistries.BLOCKS, QCraft.MOD_ID);
	
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> OAK_PLATE = registerWoodenPlate("oak_plate");
//
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> BIRCH_PLATE = registerWoodenPlate("birch_plate");
//
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> ACACIA_PLATE = registerWoodenPlate("acacia_plate");
//
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> JUNGLE_PLATE = registerWoodenPlate("jungle_plate");
//
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> SPRUCE_PLATE = registerWoodenPlate("spruce_plate");
//
//	@SuppressWarnings("unused")
//	public static final RegistryObject<Block> DARK_OAK_PLATE = registerWoodenPlate("dark_oak_plate");
	
	public static final RegistryObject<Block> PUZZLE_BOX_EASY = registerPuzzleLootBox("puzzle_box_easy", 2, LootBoxes.PUZZLE_BOX_EASY, PuzzleTypes.PUZZLE_EASY);

	public static final RegistryObject<Block> PUZZLE_BOX_NORMAL = registerPuzzleLootBox("puzzle_box_normal", 4, LootBoxes.PUZZLE_BOX_NORMAL, PuzzleTypes.PUZZLE_NORMAL);

	public static final RegistryObject<Block> PUZZLE_BOX_HARD = registerPuzzleLootBox("puzzle_box_hard", 6, LootBoxes.PUZZLE_BOX_HARD, PuzzleTypes.PUZZLE_HARD);
	
	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PANDORAS_BOX = registerLootBox("pandoras_box", LootBoxes.PANDORAS_BOX);
	
	@SuppressWarnings("unused")
	public static final RegistryObject<Block> LOOT_BOX_GENERATOR_BLOCK = register(
			"loot_box_generator",
			() -> new LootBoxGeneratorBlock(
					BlockBehaviour.Properties
							.of(Material.STONE, MaterialColor.COLOR_BLACK)
							.strength(5.0f, 1200f)
			),
			CreativeModeTab.TAB_MISC
	);

	@SuppressWarnings("unused")
	private static RegistryObject<Block> registerWoodenPlate(String name) {
		return register(
				name,
				() -> new PlateBlock(
						BlockBehaviour.Properties
								.of(Material.WOOD, MaterialColor.WOOD)
								.strength(0.6f, 0.9f)
								.sound(SoundType.WOOD)
								.noOcclusion()
				),
				CreativeModeTab.TAB_MATERIALS
		);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<Block> registerLootBox(String name, LootBox lootBox) {
		return register(
				name,
				() -> new LootBoxBlock(
						BlockBehaviour.Properties
								.of(Material.WOOD, MaterialColor.WOOD)
								.strength(1.0f, 5.0f)
								.sound(SoundType.WOOD),
						lootBox
				),
				new Item.Properties()
						.tab(CreativeModeTab.TAB_MISC)
						.rarity(Rarity.EPIC)
		);
	}
	
	private static RegistryObject<Block> registerPuzzleLootBox(String name, int explosionPower, LootBox lootBox, PuzzleType config) {
		return register(
				name,
				() -> new PuzzleBoxBlock(
						BlockBehaviour.Properties
								.of(Material.WOOD)
								.strength(1.0f, 5.0f)
								.sound(SoundType.WOOD),
						lootBox,
						explosionPower,
						config
				),
				block -> new PuzzleBoxBlockItem(
						block,
						new Item.Properties().tab(CreativeModeTab.TAB_MISC).rarity(Rarity.EPIC)
				),
				true
		);
	}
	
	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
	
	@SuppressWarnings("unused")
	private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> blockSupplier) {
		return register(name, blockSupplier, block -> null, false);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier, CreativeModeTab itemGroup) {
		
		return register(name, blockSupplier, new Item.Properties().tab(itemGroup));
	}
	
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier, Item.Properties blockItemProperties) {
		
		return register(name, blockSupplier, block -> new BlockItem(block, blockItemProperties), true);
	}
	
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
