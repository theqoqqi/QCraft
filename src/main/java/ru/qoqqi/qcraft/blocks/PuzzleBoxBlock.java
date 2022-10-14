package ru.qoqqi.qcraft.blocks;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.containers.PuzzleBoxMenu;
import ru.qoqqi.qcraft.items.PuzzleBoxBlockItem;
import ru.qoqqi.qcraft.puzzles.PuzzleType;
import ru.qoqqi.qcraft.blockentities.PuzzleBoxBlockEntity;

public class PuzzleBoxBlock extends BaseEntityBlock {
	
	private static final Component CONTAINER_NAME = Component.translatable("container.crafting");
	
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
	public InteractionResult use(@Nonnull BlockState state, Level level, @Nonnull BlockPos blockPos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
		if (level.isClientSide) {
			lastActivatedPos = blockPos;
			lastActivatedBlock = this;
			return InteractionResult.SUCCESS;
			
		} else if (player.containerMenu instanceof PuzzleBoxMenu puzzleBoxContainer) {
			
			if (puzzleBoxContainer.isSolutionSlotsFilled()) {
				BlockEntity blockEntity = level.getBlockEntity(blockPos);
				
				if (blockEntity instanceof PuzzleBoxBlockEntity puzzleBoxBlockEntity) {
					List<ItemStack> solutionItems = puzzleBoxContainer.getSolutionItems();
					
					puzzleBoxBlockEntity.onTriedToSolve(level, blockPos, player, solutionItems);
				}
			}
			
			return InteractionResult.CONSUME;
			
		} else {
			player.openMenu(state.getMenuProvider(level, blockPos));
			return InteractionResult.CONSUME;
		}
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pPos, @NotNull BlockState pState) {
		return new PuzzleBoxBlockEntity(pPos, pState);
	}
	
	@Override
	public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity placer, @Nonnull ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		
		PuzzleBoxBlockEntity blockEntity = (PuzzleBoxBlockEntity) level.getBlockEntity(pos);
		PuzzleBoxBlockItem blockItem = ((PuzzleBoxBlockItem) asItem());
		long seed = level.isClientSide
				? blockItem.getSeed(stack)
				: blockItem.getOrCreateSeed(stack, level);
		
		if (blockEntity != null) {
			blockEntity.setSeed(seed);
		}
	}
	
	@Override
	public MenuProvider getMenuProvider(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos) {
		return new SimpleMenuProvider((id, inventory, player) -> {
			PuzzleBoxBlockEntity blockEntity = (PuzzleBoxBlockEntity) level.getBlockEntity(pos);
			return new PuzzleBoxMenu(
					id,
					player.getInventory(),
					ContainerLevelAccess.create(level, pos),
					this,
					blockEntity == null ? null : blockEntity.getInventory()
			);
		}, CONTAINER_NAME);
	}
	
	@SuppressWarnings("deprecation")
	@NotNull
	public RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
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
	
	public static long getSeedFor(Level level, BlockPos blockPos) {
		PuzzleBoxBlockEntity blockEntity = (PuzzleBoxBlockEntity) level.getBlockEntity(blockPos);
		
		if (blockEntity == null) {
			return 0;
		}
		
		return blockEntity.getSeed();
	}
}
