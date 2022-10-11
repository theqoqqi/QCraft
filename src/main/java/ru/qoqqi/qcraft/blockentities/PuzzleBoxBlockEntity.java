package ru.qoqqi.qcraft.blockentities;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;
import ru.qoqqi.qcraft.puzzles.CraftingPuzzle;
import ru.qoqqi.qcraft.puzzles.PuzzleType;

public class PuzzleBoxBlockEntity extends BlockEntity {
	
	/*
	 * Больше информации о создании TileEntity здесь
	 * https://mcforge.readthedocs.io/en/1.16.x/tileentities/tileentity/
	 * */
	
	public static final int inventorySize = 27;
	
	private final SimpleContainer inventory = new SimpleContainer(inventorySize);
	
	private long seed;
	
	private int numPlayersUsing;
	
	private CraftingPuzzle puzzle;
	
	public PuzzleBoxBlockEntity(BlockPos pPos, BlockState pBlockState) {
		this(ModBlockEntityTypes.PUZZLE_BOX.get(), pPos, pBlockState);
	}
	
	public PuzzleBoxBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
		super(pType, pPos, pBlockState);
	}
	
	public long getSeed() {
		return seed;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		this.puzzle = null;
		
		setChanged();
	}
	
	public CraftingPuzzle getPuzzle() {
		if (puzzle != null) {
			return puzzle;
		}
		
		if (level == null) {
			return null;
		}
		
		BlockPos pos = getBlockPos();
		Block block = level.getBlockState(pos).getBlock();
		
		if (!(block instanceof PuzzleBoxBlock puzzleBoxBlock)) {
			return null;
		}
		
		Random random = new Random(PuzzleBoxBlock.getSeedFor(level, pos));
		PuzzleType config = puzzleBoxBlock.getPuzzleType();
		
		puzzle = CraftingPuzzle.generate(level, random, config);
		
		return puzzle;
	}
	
	public List<ItemStack> makeIngredients() {
		return getPuzzle()
				.getIngredients()
				.stream()
				.map(ItemStack::copy)
				.collect(Collectors.toList());
	}
	
	public void onTriedToSolve(Level level, BlockPos pos, Player player, List<ItemStack> solutionItems) {
		MinecraftServer server = level.getServer();
		
		if (server == null) {
			return;
		}
		
		PuzzleBoxBlock block = (PuzzleBoxBlock) getBlockState().getBlock();
		
		if (getPuzzle().isCorrectSolution(solutionItems)) {
			solve(level, pos, player, block);
		} else {
			explode(level, pos, player, server, block);
		}
	}
	
	private void solve(Level level, BlockPos pos, Player player, PuzzleBoxBlock block) {
		ItemStack itemStack = new ItemStack(block.asItem());
		
		ModCriteriaTriggers.SOLVE_PUZZLE.trigger((ServerPlayer) player, pos);
		
		level.destroyBlock(pos, false);
		
		block.getLootBox().openWithActionResult(player, itemStack, pos);
		
		syncInventory((ServerPlayer) player);
	}
	
	private void explode(Level level, BlockPos pos, Player player, MinecraftServer server, PuzzleBoxBlock block) {
		double x = pos.getX();
		double y = pos.getY();
		double z = pos.getZ();
		
		level.destroyBlock(pos, false);
		level.explode(null, x, y, z, block.getExplosionPower(), Explosion.BlockInteraction.BREAK);
		
		Component message = Component.translatable(
				"puzzleBox.exploded",
				player.getDisplayName()
		);
		
		server.getPlayerList().broadcastSystemToAllExceptTeam(player, message);
	}
	
	private void syncInventory(ServerPlayer player) {
		int containerId = ClientboundContainerSetSlotPacket.PLAYER_INVENTORY;
		NonNullList<ItemStack> inventoryItems = player.getInventory().items;
		
		for (int i = 0; i < inventoryItems.size(); i++) {
			ItemStack itemStack = inventoryItems.get(i);
			ClientboundContainerSetSlotPacket packet =
					new ClientboundContainerSetSlotPacket(containerId, 0, i, itemStack);
			
			player.connection.send(packet);
		}
	}
	
	public SimpleContainer getInventory() {
		return inventory;
	}
	
	public void fillInventory(List<ItemStack> contents) {
		inventory.removeAllItems();
		
		for (int i = 0; i < contents.size(); i++) {
			ItemStack stack = contents.get(i).copy();
			inventory.setItem(i, stack);
		}
		
		setChanged();
	}
	
	public boolean isEmpty() {
		return inventory.isEmpty();
	}
	
	public boolean triggerEvent(int id, int type) {
		if (id == 1) {
			this.numPlayersUsing = type;
			return true;
		} else {
			return super.triggerEvent(id, type);
		}
	}
	
	public void openInventory(Player player) {
		if (player.isSpectator()) {
			return;
		}
		
		if (this.numPlayersUsing < 0) {
			this.numPlayersUsing = 0;
		}
		
		++this.numPlayersUsing;
		this.onOpenOrClose();
	}
	
	public void closeInventory(Player player) {
		if (player.isSpectator()) {
			return;
		}
		
		--this.numPlayersUsing;
		this.onOpenOrClose();
	}
	
	protected void onOpenOrClose() {
		Block block = this.getBlockState().getBlock();
		
		if (this.level != null && block instanceof ChestBlock) {
			this.level.blockEvent(this.getBlockPos(), block, 1, this.numPlayersUsing);
			this.level.updateNeighborsAt(this.getBlockPos(), block);
		}
		
		if (numPlayersUsing == 0) {
			inventory.removeAllItems();
		}
	}
	
	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		
		tag.putLong("seed", seed);
		
		NonNullList<ItemStack> contents = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
		for (int i = 0; i < contents.size(); i++) {
			contents.set(i, inventory.getItem(i));
		}
		ContainerHelper.saveAllItems(tag, contents);
	}
	
	@Override
	public void load(@NotNull CompoundTag tag) {
		super.load(tag);
		
		setSeed(tag.getLong("seed"));
		
		NonNullList<ItemStack> contents = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
		ContainerHelper.loadAllItems(tag, contents);
		fillInventory(contents);
	}
}
