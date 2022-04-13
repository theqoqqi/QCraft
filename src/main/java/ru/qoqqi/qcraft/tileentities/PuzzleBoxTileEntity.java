package ru.qoqqi.qcraft.tileentities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;
import ru.qoqqi.qcraft.puzzles.CraftingPuzzle;
import ru.qoqqi.qcraft.puzzles.PuzzleType;

public class PuzzleBoxTileEntity extends TileEntity {
	
	/*
	 * Больше информации о создании TileEntity здесь
	 * https://mcforge.readthedocs.io/en/1.16.x/tileentities/tileentity/
	 * */
	
	public static final int inventorySize = 27;
	
	private final IInventory inventory = new Inventory(inventorySize);
	
	private long seed;
	
	private int numPlayersUsing;
	
	private CraftingPuzzle puzzle;
	
	public PuzzleBoxTileEntity() {
		this(ModTileEntityTypes.PUZZLE_BOX.get());
	}
	
	public PuzzleBoxTileEntity(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}
	
	public long getSeed() {
		return seed;
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
		this.puzzle = null;
		markDirty();
	}
	
	public CraftingPuzzle getPuzzle() {
		if (puzzle == null) {
			if (world == null) {
				return null;
			}
			
			BlockPos pos = getPos();
			Block block = world.getBlockState(pos).getBlock();
			
			if (!(block instanceof PuzzleBoxBlock)) {
				return null;
			}
			
			PuzzleBoxBlock puzzleBoxBlock = (PuzzleBoxBlock) block;
			Random random = new Random(PuzzleBoxBlock.getSeedFor(world, pos));
			PuzzleType config = puzzleBoxBlock.getPuzzleType();
			
			puzzle = CraftingPuzzle.generate(world, random, config);
		}
		
		return puzzle;
	}
	
	public List<ItemStack> makeIngredients() {
		return getPuzzle()
				.getIngredients()
				.stream()
				.map(ItemStack::copy)
				.collect(Collectors.toList());
	}
	
	public void onTriedToSolve(World world, BlockPos pos, PlayerEntity player, List<ItemStack> solutionItems) {
		MinecraftServer server = world.getServer();
		
		if (server == null) {
			return;
		}
		
		PuzzleBoxBlock block = (PuzzleBoxBlock) getBlockState().getBlock();
		
		if (getPuzzle().isCorrectSolution(solutionItems)) {
			ItemStack itemStack = new ItemStack(block.asItem());
			
			world.destroyBlock(pos, false);
			
			block.getLootBox().openWithActionResult(player, itemStack, pos);
			
			syncInventory((ServerPlayerEntity) player);
			
		} else {
			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();
			
			world.destroyBlock(pos, false);
			world.createExplosion(null, x, y, z, block.getExplosionPower(), Explosion.Mode.BREAK);
			
			ITextComponent message = new TranslationTextComponent(
					"puzzleBox.exploded",
					player.getDisplayName()
			);
			
			server.getPlayerList().sendMessageToTeamOrAllPlayers(player, message);
		}
	}
	
	private void syncInventory(ServerPlayerEntity player) {
		for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
			ItemStack itemStack2 = player.inventory.mainInventory.get(i);
			player.connection.sendPacket(new SSetSlotPacket(-2, i, itemStack2));
		}
	}
	
	public IInventory getInventory() {
		return inventory;
	}
	
	public void fillInventory(List<ItemStack> contents) {
		inventory.clear();
		
		for (int i = 0; i < contents.size(); i++) {
			ItemStack stack = contents.get(i).copy();
			inventory.setInventorySlotContents(i, stack);
		}
		
		markDirty();
	}
	
	public boolean isEmpty() {
		return inventory.isEmpty();
	}
	
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.numPlayersUsing = type;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}
	
	public void openInventory(PlayerEntity player) {
		if (player.isSpectator()) {
			return;
		}
		
		if (this.numPlayersUsing < 0) {
			this.numPlayersUsing = 0;
		}
		
		++this.numPlayersUsing;
		this.onOpenOrClose();
	}
	
	public void closeInventory(PlayerEntity player) {
		if (player.isSpectator()) {
			return;
		}
		
		--this.numPlayersUsing;
		this.onOpenOrClose();
	}
	
	protected void onOpenOrClose() {
		Block block = this.getBlockState().getBlock();
		
		if (this.world != null && block instanceof ChestBlock) {
			this.world.addBlockEvent(this.pos, block, 1, this.numPlayersUsing);
			this.world.notifyNeighborsOfStateChange(this.pos, block);
		}
		
		if (numPlayersUsing == 0) {
			inventory.clear();
		}
	}
	
	@Override
	public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
		super.read(state, nbt);
		
		setSeed(nbt.getLong("seed"));
		
		NonNullList<ItemStack> contents = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(nbt, contents);
		fillInventory(contents);
	}
	
	@Nonnull
	@Override
	public CompoundNBT write(@Nonnull CompoundNBT nbt) {
		super.write(nbt);
		
		nbt.putLong("seed", seed);
		
		NonNullList<ItemStack> contents = NonNullList.withSize(inventorySize, ItemStack.EMPTY);
		for (int i = 0; i < contents.size(); i++) {
			contents.set(i, inventory.getStackInSlot(i));
		}
		ItemStackHelper.saveAllItems(nbt, contents);
		
		return nbt;
	}
}
