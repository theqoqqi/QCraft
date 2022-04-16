package ru.qoqqi.qcraft.tileentities;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.function.Supplier;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.blocks.ModBlocks;

public class ModTileEntityTypes {
	
	public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES
			= DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, QCraft.MOD_ID);

	public static final RegistryObject<TileEntityType<PuzzleBoxTileEntity>> PUZZLE_BOX = register(
			"puzzle_box",
			PuzzleBoxTileEntity::new,
			ModBlocks.PUZZLE_BOX_EASY,
			ModBlocks.PUZZLE_BOX_NORMAL,
			ModBlocks.PUZZLE_BOX_HARD
	);
	
	public static final RegistryObject<TileEntityType<LootBoxGeneratorTileEntity>> LOOT_BOX_GENERATOR = register(
			"loot_box_generator",
			LootBoxGeneratorTileEntity::new,
			ModBlocks.LOOT_BOX_GENERATOR_BLOCK
	);
	
	@SafeVarargs
	private static <T extends TileEntity> RegistryObject<TileEntityType<T>> register(String name, Supplier<T> supplier, RegistryObject<Block>... validBlocks) {
		return TILE_ENTITIES.register(name, () -> {
			Block[] blocks = Arrays.stream(validBlocks).map(RegistryObject::get).toArray(Block[]::new);
			
			//noinspection ConstantConditions
			return TileEntityType.Builder.create(supplier, blocks).build(null);
		});
	}
	
	public static void register(IEventBus eventBus) {
		TILE_ENTITIES.register(eventBus);
	}
}
