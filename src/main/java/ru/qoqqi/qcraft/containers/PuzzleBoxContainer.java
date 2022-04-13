package ru.qoqqi.qcraft.containers;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blocks.ModBlocks;
import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;
import ru.qoqqi.qcraft.tileentities.PuzzleBoxTileEntity;

public class PuzzleBoxContainer extends Container {
	
	private static final int CRAFTING_MATRIX_X = 30;
	
	private static final int CRAFTING_MATRIX_Y = 17;
	
	private static final int CRAFT_RESULT_X = 124;
	
	private static final int CRAFT_RESULT_Y = 35;
	
	private static final int MAIN_INVENTORY_X = 8;
	
	private static final int MAIN_INVENTORY_Y = 84;
	
	private static final int SOLUTION_INVENTORY_X = 17;
	
	private static final int SOLUTION_INVENTORY_Y = 159;
	
	private static final int SOLUTION_INVENTORY_SPACING = 36;
	
	private final CraftingInventory craftingInventory = new CraftingInventory(this, 3, 3);
	
	private final CraftResultInventory craftResultInventory = new CraftResultInventory();
	
	private final IInventory mainInventory;
	
	private final IInventory solutionInventory = new Inventory(3);
	
	private final IWorldPosCallable worldPosCallable;
	
	private final PlayerEntity player;
	
	private final Slot craftResultSlot;
	
	private final List<Slot> craftingInventorySlots;
	
	private final List<Slot> mainInventorySlots;
	
	private final List<Slot> solutionInventorySlots;
	
	private final PuzzleBoxBlock block;
	
	private PuzzleBoxTileEntity tileEntity;
	
	public PuzzleBoxContainer(int id, PlayerInventory playerInventory) {
		this(id, playerInventory, IWorldPosCallable.DUMMY, (PuzzleBoxBlock) ModBlocks.PUZZLE_BOX_EASY.get(), null);
	}
	
	public PuzzleBoxContainer(int id, @Nonnull PlayerInventory playerInventory, IWorldPosCallable worldPosCallable, PuzzleBoxBlock block, IInventory mainInventory) {
		super(ModContainers.PUZZLE_BOX_CONTAINER.get(), id);
		this.worldPosCallable = worldPosCallable;
		this.player = playerInventory.player;
		this.block = block;
		this.mainInventory = mainInventory == null
				? new Inventory(PuzzleBoxTileEntity.inventorySize)
				: mainInventory;
		
		craftResultSlot = addCraftResultSlot(playerInventory);
		craftingInventorySlots = addCraftingInventorySlots();
		mainInventorySlots = addMainInventorySlots();
		solutionInventorySlots = addSolutionInventorySlots();
		
		this.worldPosCallable.consume(((world, blockPos) -> {
			tileEntity = (PuzzleBoxTileEntity) world.getTileEntity(blockPos);
			
			if (tileEntity == null) {
				return;
			}
			
			if (tileEntity.isEmpty()) {
				List<ItemStack> ingredients = tileEntity.makeIngredients();
				
				for (ItemStack stack : ingredients) {
					merge(stack, mainInventorySlots);
				}
				
				List<ItemStack> contents = mainInventorySlots.stream()
						.map(Slot::getStack)
						.collect(Collectors.toList());
				
				tileEntity.fillInventory(contents);
			}
			
			tileEntity.openInventory(player);
		}));
	}
	
	@Nonnull
	private List<Slot> addCraftingInventorySlots() {
		List<Slot> slots = new ArrayList<>();
		
		for (int slotY = 0; slotY < 3; ++slotY) {
			for (int slotX = 0; slotX < 3; ++slotX) {
				int index = slotX + slotY * 3;
				int x = CRAFTING_MATRIX_X + slotX * 18;
				int y = CRAFTING_MATRIX_Y + slotY * 18;
				
				Slot slot = new Slot(craftingInventory, index, x, y);
				
				addSlot(slot);
				slots.add(slot);
			}
		}
		
		return slots;
	}
	
	@Nonnull
	private Slot addCraftResultSlot(@Nonnull PlayerInventory playerInventory) {
		CraftingResultSlot slot = new CraftingResultSlot(
				playerInventory.player,
				craftingInventory,
				craftResultInventory,
				0,
				CRAFT_RESULT_X,
				CRAFT_RESULT_Y
		);
		
		return addSlot(slot);
	}
	
	@Nonnull
	private List<Slot> addMainInventorySlots() {
		List<Slot> slots = new ArrayList<>();
		
		for (int slotY = 0; slotY < 3; ++slotY) {
			for (int slotX = 0; slotX < 9; ++slotX) {
				int index = slotX + slotY * 9;
				int x = MAIN_INVENTORY_X + slotX * 18;
				int y = MAIN_INVENTORY_Y + slotY * 18;
				
				Slot slot = new Slot(mainInventory, index, x, y);
				
				addSlot(slot);
				slots.add(slot);
			}
		}
		
		return slots;
	}
	
	@Nonnull
	private List<Slot> addSolutionInventorySlots() {
		List<Slot> slots = new ArrayList<>();
		
		for (int i = 0; i < 3; ++i) {
			int x = SOLUTION_INVENTORY_X + i * SOLUTION_INVENTORY_SPACING;
			
			Slot slot = new Slot(solutionInventory, i, x, SOLUTION_INVENTORY_Y);
			
			addSlot(slot);
			slots.add(slot);
		}
		
		return slots;
	}
	
	@Override
	public void onContainerClosed(@Nonnull PlayerEntity player) {
		player.inventory.setItemStack(ItemStack.EMPTY);
		if (tileEntity != null) {
			tileEntity.closeInventory(player);
		}
		super.onContainerClosed(player);
	}
	
	@Override
	protected void clearContainer(@Nonnull PlayerEntity player, @Nonnull World world, @Nonnull IInventory inventory) {
		// Do not drop or return items to prevent dupe
	}
	
	@Override
	public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
		return slotIn != craftResultSlot && super.canMergeSlot(stack, slotIn);
	}
	
	@Nonnull
	@Override
	public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull PlayerEntity player) {
		boolean shouldIgnore = clickType == ClickType.SWAP
				|| clickType == ClickType.THROW
				|| clickType == ClickType.CLONE
				|| clickType == ClickType.PICKUP && slotId == -999;
		
		if (shouldIgnore) {
			return ItemStack.EMPTY;
		}
		
		return super.slotClick(slotId, dragType, clickType, player);
	}
	
	@Override
	public void onCraftMatrixChanged(@Nonnull IInventory inventory) {
		this.worldPosCallable.consume((world, blockPos) -> {
			updateCraftingResult(this.windowId, world, player, craftingInventory, craftResultInventory);
		});
	}
	
	private void updateCraftingResult(int id, @Nonnull World world, PlayerEntity player, CraftingInventory inventory, CraftResultInventory inventoryResult) {
		MinecraftServer server = world.getServer();
		
		if (world.isRemote || server == null) {
			return;
		}
		
		ServerPlayerEntity playerEntity = (ServerPlayerEntity) player;
		ItemStack itemStack = ItemStack.EMPTY;
		RecipeManager recipeManager = server.getRecipeManager();
		Optional<ICraftingRecipe> optional = recipeManager.getRecipe(IRecipeType.CRAFTING, inventory, world);
		
		if (optional.isPresent()) {
			ICraftingRecipe recipe = optional.get();
			if (inventoryResult.canUseRecipe(world, playerEntity, recipe)) {
				itemStack = recipe.getCraftingResult(inventory);
			}
		}
		
		int slotIndex = craftResultSlot.getSlotIndex();
		inventoryResult.setInventorySlotContents(0, itemStack);
		playerEntity.connection.sendPacket(new SSetSlotPacket(id, slotIndex, itemStack));
	}
	
	@Nonnull
	@Override
	public ItemStack transferStackInSlot(@Nonnull PlayerEntity player, int index) {
		Slot slot = this.inventorySlots.get(index);
		
		if (slot == null || !slot.getHasStack()) {
			return ItemStack.EMPTY;
		}
		
		ItemStack oldItemStack = slot.getStack();
		ItemStack newItemStack = oldItemStack.copy();
		
		if (!mergeSlot(player, slot, oldItemStack, newItemStack)) {
			return ItemStack.EMPTY;
		}
		
		if (oldItemStack.isEmpty()) {
			slot.putStack(ItemStack.EMPTY);
		} else {
			slot.onSlotChanged();
		}
		
		if (oldItemStack.getCount() == newItemStack.getCount()) {
			return ItemStack.EMPTY;
		}
		
		slot.onTake(player, oldItemStack);
		
		return newItemStack;
	}
	
	private boolean mergeSlot(@Nonnull PlayerEntity player, Slot slot, ItemStack oldItemStack, ItemStack newItemStack) {
		if (slot == craftResultSlot) {
			return mergeCraftResult(player, slot, oldItemStack, newItemStack);
		}
		
		if (craftingInventorySlots.contains(slot)) {
			return merge(oldItemStack, mainInventorySlots);
		}
		
		if (mainInventorySlots.contains(slot)) {
			return merge(oldItemStack, solutionInventorySlots);
		}
		
		if (solutionInventorySlots.contains(slot)) {
			return merge(oldItemStack, mainInventorySlots);
		}
		
		return true;
	}
	
	private boolean mergeCraftResult(@Nonnull PlayerEntity player, Slot slot, ItemStack oldItemStack, ItemStack newItemStack) {
		this.worldPosCallable.consume((world, blockPos) -> {
			oldItemStack.getItem().onCreated(oldItemStack, world, player);
		});
		
		if (!merge(oldItemStack, mainInventorySlots)) {
			return false;
		}
		
		slot.onSlotChange(oldItemStack, newItemStack);
		return true;
	}
	
	public List<ItemStack> getSolutionItems() {
		return solutionInventorySlots.stream().map(Slot::getStack).collect(Collectors.toList());
	}
	
	private boolean merge(ItemStack itemStack, @Nonnull List<Slot> toSlots) {
		int fromIndex = toSlots.get(0).slotNumber;
		int toIndex = toSlots.get(toSlots.size() - 1).slotNumber + 1;
		
		return mergeItemStack(itemStack, fromIndex, toIndex, false);
	}
	
	@Override
	public boolean canInteractWith(@Nonnull PlayerEntity player) {
		return isWithinUsableDistance(worldPosCallable, player, block);
	}
	
	public boolean isSolutionSlotsFilled() {
		return solutionInventorySlots.stream().allMatch(slot -> {
			return !slot.getStack().isEmpty();
		});
	}
}