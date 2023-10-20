package ru.qoqqi.qcraft.containers;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blockentities.PuzzleBoxBlockEntity;
import ru.qoqqi.qcraft.blocks.PuzzleBoxBlock;

public class PuzzleBoxMenu extends AbstractContainerMenu {

	private static final int CRAFTING_MATRIX_X = 30;

	private static final int CRAFTING_MATRIX_Y = 17;

	private static final int CRAFT_RESULT_X = 124;

	private static final int CRAFT_RESULT_Y = 35;

	private static final int MAIN_INVENTORY_X = 8;

	private static final int MAIN_INVENTORY_Y = 84;

	private final CraftingContainer craftingInventory = new TransientCraftingContainer(this, 3, 3);

	private final ResultContainer craftResultInventory = new ResultContainer();

	private final SimpleContainer mainInventory;

	private final SimpleContainer solutionInventory;

	private final ContainerLevelAccess containerLevelAccess;

	private final Player player;

	private final Slot craftResultSlot;

	private final List<Slot> craftingInventorySlots;

	private final List<Slot> mainInventorySlots;

	private final List<Slot> solutionInventorySlots;

	private final PuzzleBoxBlock block;

	public final int solutionInventoryX;

	public final int solutionInventoryY;

	public final int solutionInventorySpacing;

	private PuzzleBoxBlockEntity blockEntity;

	public PuzzleBoxMenu(int id, Inventory playerInventory) {
		this(id, playerInventory, ContainerLevelAccess.NULL, PuzzleBoxBlock.getLastActivatedBlock(), null);
	}

	public PuzzleBoxMenu(int id, @Nonnull Inventory playerInventory, ContainerLevelAccess containerLevelAccess, PuzzleBoxBlock block, SimpleContainer mainInventory) {
		super(ModMenus.PUZZLE_BOX_MENU.get(), id);
		int solutionSize = block.getPuzzleType().solutionSize;

		this.containerLevelAccess = containerLevelAccess;
		this.player = playerInventory.player;
		this.block = block;
		this.mainInventory = mainInventory == null
				? new SimpleContainer(PuzzleBoxBlockEntity.inventorySize)
				: mainInventory;
		this.solutionInventory = new SimpleContainer(solutionSize);

		solutionInventoryX = getSolutionInventoryX(solutionSize);
		solutionInventoryY = getSolutionInventoryY(solutionSize);
		solutionInventorySpacing = getSolutionInventorySpacing(solutionSize);

		craftResultSlot = addCraftResultSlot(playerInventory);
		craftingInventorySlots = addCraftingInventorySlots();
		mainInventorySlots = addMainInventorySlots();
		solutionInventorySlots = addSolutionInventorySlots();

		this.containerLevelAccess.execute(((level, blockPos) -> {
			blockEntity = (PuzzleBoxBlockEntity) level.getBlockEntity(blockPos);

			if (blockEntity == null) {
				return;
			}

			if (blockEntity.isEmpty()) {
				List<ItemStack> ingredients = blockEntity.makeIngredients();

				for (ItemStack stack : ingredients) {
					merge(stack, mainInventorySlots);
				}

				List<ItemStack> contents = mainInventorySlots.stream()
						.map(Slot::getItem)
						.collect(Collectors.toList());

				blockEntity.fillInventory(contents);
			}

			blockEntity.openInventory(player);
		}));
	}

	private int getSolutionInventoryX(int solutionSize) {
		solutionSize -= 3;
		return 17 - solutionSize * 2;
	}

	private int getSolutionInventoryY(int solutionSize) {
		return 159;
	}

	private int getSolutionInventorySpacing(int solutionSize) {
		solutionSize -= 3;
		return 36 - solutionSize * 5;
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
	private Slot addCraftResultSlot(@Nonnull Inventory playerInventory) {
		ResultSlot slot = new ResultSlot(
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

		for (int i = 0; i < solutionInventory.getContainerSize(); ++i) {
			int x = solutionInventoryX + i * solutionInventorySpacing;

			Slot slot = new Slot(solutionInventory, i, x, solutionInventoryY);

			addSlot(slot);
			slots.add(slot);
		}

		return slots;
	}

	@Override
	public void removed(@Nonnull Player player) {
		setCarried(ItemStack.EMPTY);
		if (blockEntity != null) {
			blockEntity.closeInventory(player);
		}
		super.removed(player);
	}

	@Override
	protected void clearContainer(@Nonnull Player player, @Nonnull Container inventory) {
		// Do not drop or return items to prevent dupe
	}

	@Override
	public boolean canTakeItemForPickAll(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
		return slotIn != craftResultSlot && super.canTakeItemForPickAll(stack, slotIn);
	}

	@Override
	public void clicked(int slotId, int dragType, @Nonnull ClickType clickType, @Nonnull Player player) {
		boolean shouldIgnore = clickType == ClickType.SWAP
				|| clickType == ClickType.THROW
				|| clickType == ClickType.CLONE
				|| clickType == ClickType.PICKUP && slotId == -999;

		if (shouldIgnore) {
			return;
		}

		super.clicked(slotId, dragType, clickType, player);
	}

	@Override
	public void slotsChanged(@Nonnull Container inventory) {
		this.containerLevelAccess.execute((level, blockPos) -> {
			updateCraftingResult(this.containerId, level, player, craftingInventory, craftResultInventory);
		});
	}

	private void updateCraftingResult(int id, @Nonnull Level level, Player player, CraftingContainer inventory, ResultContainer inventoryResult) {
		MinecraftServer server = level.getServer();

		if (level.isClientSide || server == null) {
			return;
		}

		ServerPlayer playerEntity = (ServerPlayer) player;
		ItemStack itemStack = ItemStack.EMPTY;
		RecipeManager recipeManager = server.getRecipeManager();
		Optional<CraftingRecipe> optional = recipeManager.getRecipeFor(RecipeType.CRAFTING, inventory, level);

		if (optional.isPresent()) {
			CraftingRecipe recipe = optional.get();
			if (inventoryResult.setRecipeUsed(level, playerEntity, recipe)) {
				itemStack = recipe.assemble(inventory, level.registryAccess());
			}
		}

		int slotIndex = craftResultSlot.getSlotIndex();
		inventoryResult.setItem(0, itemStack);
		playerEntity.connection.send(new ClientboundContainerSetSlotPacket(id, 0, slotIndex, itemStack));
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player player, int index) {
		Slot slot = this.slots.get(index);

		if (!slot.hasItem()) {
			return ItemStack.EMPTY;
		}

		ItemStack oldItemStack = slot.getItem();
		ItemStack newItemStack = oldItemStack.copy();

		if (!mergeSlot(player, slot, oldItemStack, newItemStack)) {
			return ItemStack.EMPTY;
		}

		if (oldItemStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}

		if (oldItemStack.getCount() == newItemStack.getCount()) {
			return ItemStack.EMPTY;
		}

		slot.onTake(player, oldItemStack);

		return newItemStack;
	}

	private boolean mergeSlot(@Nonnull Player player, Slot slot, ItemStack oldItemStack, ItemStack newItemStack) {
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

	private boolean mergeCraftResult(@Nonnull Player player, Slot slot, ItemStack oldItemStack, ItemStack newItemStack) {
		this.containerLevelAccess.execute((level, blockPos) -> {
			oldItemStack.getItem().onCraftedBy(oldItemStack, level, player);
		});

		if (!merge(oldItemStack, mainInventorySlots)) {
			return false;
		}

		slot.onQuickCraft(oldItemStack, newItemStack);
		return true;
	}

	public List<ItemStack> getSolutionItems() {
		return solutionInventorySlots.stream().map(Slot::getItem).collect(Collectors.toList());
	}

	private boolean merge(ItemStack itemStack, @Nonnull List<Slot> toSlots) {
		int fromIndex = toSlots.get(0).index;
		int toIndex = toSlots.get(toSlots.size() - 1).index + 1;

		return moveItemStackTo(itemStack, fromIndex, toIndex, false);
	}

	@Override
	public boolean stillValid(@Nonnull Player player) {
		return stillValid(containerLevelAccess, player, block);
	}

	public boolean isSolutionSlotsFilled() {
		return solutionInventorySlots.stream().allMatch(slot -> {
			return !slot.getItem().isEmpty();
		});
	}

	public int getSolutionSize() {
		return solutionInventory.getContainerSize();
	}
}