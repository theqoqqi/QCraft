package ru.qoqqi.qcraft.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.extensions.IForgeBlock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.containers.PuzzleBoxContainer;
import ru.qoqqi.qcraft.items.PuzzleBoxBlockItem;
import ru.qoqqi.qcraft.puzzles.CraftingPuzzle;
import ru.qoqqi.qcraft.puzzles.PuzzleType;
import ru.qoqqi.qcraft.tileentities.PuzzleBoxTileEntity;

public class PuzzleBoxBlock extends Block {
	
	private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.crafting");
	
	private static PuzzleBoxBlock lastActivatedBlock;
	
	private static BlockPos lastActivatedPos;
	
	private final LootBox lootBox;
	
	private final int explosionPower;
	
	private final PuzzleType puzzleType;
	
	public PuzzleBoxBlock(Properties properties, LootBox lootBox, int explosionPower, PuzzleType config) {
		super(properties);
		this.lootBox = lootBox;
		this.puzzleType = config;
		this.explosionPower = explosionPower;
	}
	
	@SuppressWarnings("deprecation")
	@Nonnull
	public ActionResultType onBlockActivated(@Nonnull BlockState state, World world, @Nonnull BlockPos blockPos, @Nonnull PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit) {
		if (world.isRemote) {
			lastActivatedPos = blockPos;
			lastActivatedBlock = this;
			return ActionResultType.SUCCESS;
			
		} else if (player.openContainer instanceof PuzzleBoxContainer) {
			PuzzleBoxContainer puzzleBoxContainer = (PuzzleBoxContainer) player.openContainer;
			
			if (puzzleBoxContainer.isSolutionSlotsFilled()) {
				TileEntity tileEntity = world.getTileEntity(blockPos);
				
				if (tileEntity instanceof PuzzleBoxTileEntity) {
					PuzzleBoxTileEntity puzzleBoxTileEntity = (PuzzleBoxTileEntity) tileEntity;
					List<ItemStack> solutionItems = puzzleBoxContainer.getSolutionItems();
					
					puzzleBoxTileEntity.onTriedToSolve(world, blockPos, player, solutionItems);
				}
			}
			
			return ActionResultType.CONSUME;
			
		} else {
			player.openContainer(state.getContainer(world, blockPos));
			return ActionResultType.CONSUME;
		}
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new PuzzleBoxTileEntity();
	}
	
	@Override
	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		
		PuzzleBoxTileEntity tileEntity = (PuzzleBoxTileEntity) world.getTileEntity(pos);
		long seed = ((PuzzleBoxBlockItem) asItem()).getSeed(stack, world);
		
		if (tileEntity != null) {
			tileEntity.setSeed(seed);
		}
	}
	
	@SuppressWarnings("deprecation")
	public INamedContainerProvider getContainer(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
		return new SimpleNamedContainerProvider((id, inventory, player) -> {
			PuzzleBoxTileEntity tileEntity = (PuzzleBoxTileEntity) world.getTileEntity(pos);
			return new PuzzleBoxContainer(
					id,
					player.inventory,
					IWorldPosCallable.of(world, pos),
					this,
					tileEntity == null ? null : tileEntity.getInventory()
			);
		}, CONTAINER_NAME);
	}
	
	public LootBox getLootBox() {
		return lootBox;
	}
	
	public int getExplosionPower() {
		return explosionPower;
	}
	
	public PuzzleType getPuzzleType() {
		return puzzleType;
	}
	
	public static PuzzleBoxBlock getLastActivatedBlock() {
		return lastActivatedBlock;
	}
	
	public static BlockPos getLastActivatedPos() {
		return lastActivatedPos;
	}
	
	public static long getSeedFor(World world, BlockPos blockPos) {
		PuzzleBoxTileEntity tileEntity = (PuzzleBoxTileEntity) world.getTileEntity(blockPos);
		
		if (tileEntity == null) {
			return 0;
		}
		
		return tileEntity.getSeed();
	}
}
