package ru.qoqqi.qcraft.blocks;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
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

//	public static final RegistryObject<Block> OAK_PLATE = registerWoodenPlate("oak_plate");
//
//	public static final RegistryObject<Block> BIRCH_PLATE = registerWoodenPlate("birch_plate");
//
//	public static final RegistryObject<Block> ACACIA_PLATE = registerWoodenPlate("acacia_plate");
//
//	public static final RegistryObject<Block> JUNGLE_PLATE = registerWoodenPlate("jungle_plate");
//
//	public static final RegistryObject<Block> SPRUCE_PLATE = registerWoodenPlate("spruce_plate");
//
//	public static final RegistryObject<Block> DARK_OAK_PLATE = registerWoodenPlate("dark_oak_plate");
	
	public static final RegistryObject<Block> PUZZLE_BOX_EASY = registerPuzzleLootBox("puzzle_box_easy", 2, LootBoxes.PUZZLE_BOX_EASY, PuzzleTypes.PUZZLE_EASY);
	
	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PUZZLE_BOX_NORMAL = registerPuzzleLootBox("puzzle_box_normal", 4, LootBoxes.PUZZLE_BOX_NORMAL, PuzzleTypes.PUZZLE_NORMAL);
	
	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PUZZLE_BOX_HARD = registerPuzzleLootBox("puzzle_box_hard", 6, LootBoxes.PUZZLE_BOX_HARD, PuzzleTypes.PUZZLE_HARD);
	
	@SuppressWarnings("unused")
	public static final RegistryObject<Block> PANDORAS_BOX = registerLootBox("pandoras_box", LootBoxes.PANDORAS_BOX);
	
	@SuppressWarnings("unused")
	private static RegistryObject<Block> registerWoodenPlate(String name) {
		return register(
				name,
				() -> new PlateBlock(
						AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
								.hardnessAndResistance(0.6f, 0.9f)
								.sound(SoundType.WOOD)
								.notSolid()
				),
				ItemGroup.MATERIALS
		);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<Block> registerLootBox(String name, LootBox lootBox) {
		return register(
				name,
				() -> new LootBoxBlock(
						AbstractBlock.Properties.create(Material.WOOD, MaterialColor.WOOD)
								.hardnessAndResistance(1.0f, 5.0f)
								.sound(SoundType.WOOD),
						lootBox
				),
				new Item.Properties()
						.group(ItemGroup.MISC)
						.rarity(Rarity.EPIC)
		);
	}
	
	private static RegistryObject<Block> registerPuzzleLootBox(String name, int explosionPower, LootBox lootBox, PuzzleType config) {
		return register(
				name,
				() -> new PuzzleBoxBlock(
						AbstractBlock.Properties.create(Material.WOOD)
								.hardnessAndResistance(1.0f, 5.0f)
								.sound(SoundType.WOOD),
						lootBox,
						explosionPower,
						config
				),
				block -> new PuzzleBoxBlockItem(
						block,
						new Item.Properties().group(ItemGroup.MISC).rarity(Rarity.EPIC)
				),
				true
		);
	}
	
	public static void register(IEventBus eventBus) {
		BLOCKS.register(eventBus);
	}
	
	@SuppressWarnings("unused")
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier) {
		
		return register(name, blockSupplier, block -> null, false);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static <T extends Block> RegistryObject<T> register(
			String name, Supplier<T> blockSupplier, ItemGroup itemGroup) {
		
		return register(name, blockSupplier, new Item.Properties().group(itemGroup));
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
