package ru.qoqqi.qcraft.blockentities;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.blocks.ModBlocks;

public class ModBlockEntityTypes {
	
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES
			= DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, QCraft.MOD_ID);

	public static final RegistryObject<BlockEntityType<PuzzleBoxBlockEntity>> PUZZLE_BOX = register(
			"puzzle_box",
			PuzzleBoxBlockEntity::new,
			ModBlocks.PUZZLE_BOX_EASY,
			ModBlocks.PUZZLE_BOX_NORMAL,
			ModBlocks.PUZZLE_BOX_HARD
	);
	
	public static final RegistryObject<BlockEntityType<LootBoxGeneratorBlockEntity>> LOOT_BOX_GENERATOR = register(
			"loot_box_generator",
			LootBoxGeneratorBlockEntity::new,
			ModBlocks.LOOT_BOX_GENERATOR_BLOCK
	);
	
	public static final RegistryObject<BlockEntityType<JourneyRewardBlockEntity>> JOURNEY_REWARD = register(
			"journey_reward",
			JourneyRewardBlockEntity::new,
			ModBlocks.TRAVELERS_HOME_JOURNEY_REWARD_BLOCK,
			ModBlocks.FORTUNE_ISLAND_JOURNEY_REWARD_BLOCK,
			ModBlocks.JUNGLE_TEMPLE_JOURNEY_REWARD_BLOCK,
			ModBlocks.MANGROVE_TEMPLE_JOURNEY_REWARD_BLOCK,
			ModBlocks.PANDORAS_TEMPLE_JOURNEY_REWARD_BLOCK
	);
	
	@SafeVarargs
	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String name, BlockEntityType.BlockEntitySupplier<T> supplier, RegistryObject<Block>... validBlocks) {
		return BLOCK_ENTITY_TYPES.register(name, () -> {
			Block[] blocks = Arrays.stream(validBlocks).map(RegistryObject::get).toArray(Block[]::new);
			
			//noinspection ConstantConditions
			return BlockEntityType.Builder.of(supplier, blocks).build(null);
		});
	}
	
	public static void register(IEventBus eventBus) {
		BLOCK_ENTITY_TYPES.register(eventBus);
	}
}
