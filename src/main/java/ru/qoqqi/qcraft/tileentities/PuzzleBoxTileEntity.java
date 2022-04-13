package ru.qoqqi.qcraft.tileentities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;

import java.util.List;

import javax.annotation.Nonnull;

public class PuzzleBoxTileEntity extends TileEntity {
	
	/*
	 * Больше информации о создании TileEntity здесь
	 * https://mcforge.readthedocs.io/en/1.16.x/tileentities/tileentity/
	 * */
	
	public static final int inventorySize = 27;
	
	private final IInventory inventory = new Inventory(inventorySize);
	
	private long seed;
	
	private int numPlayersUsing;
	
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
		markDirty();
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
