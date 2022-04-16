package ru.qoqqi.qcraft.tileentities;

import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.QCraft;

public class LootBoxGeneratorTileEntity extends TileEntity implements ITickableTileEntity {
	
	private static final ResourceLocation resourceLocation = new ResourceLocation(QCraft.MOD_ID, "random_loot_box");
	
	private static final Map<World, Map<UUID, BlockPos>> activeTileEntitiesByWorlds = new HashMap<>();
	
	private static final int HOUR_IN_TICKS = 60 * 60 * 20;
	
	private static final float baseCountdownTicks = 3 * HOUR_IN_TICKS;
	
	private int age;
	
	private int countdown;
	
	private int startedFrom;
	
	public final float hoverStart;
	
	@Nonnull
	private ItemStack itemStack = ItemStack.EMPTY;
	
	private boolean isActive;
	
	private UUID playerUuid;
	
	public LootBoxGeneratorTileEntity() {
		this(ModTileEntityTypes.LOOT_BOX_GENERATOR.get());
	}
	
	public LootBoxGeneratorTileEntity(TileEntityType<?> tileEntityType) {
		super(tileEntityType);
		this.hoverStart = (float) (Math.random() * Math.PI * 2.0);
	}
	
	@Override
	public void tick() {
		age++;
		
		if (world == null) {
			return;
		}
		
		if (world.isRemote) {
			if (isActive && countdown > 0) {
				countdown--;
			}
			return;
		}
		
		if (isActive && itemStack.isEmpty()) {
			countdown--;
			
			if (countdown <= 0) {
				if (startedFrom > 0) {
					generateItemStack();
				}
				startCountdown(world.rand);
			}
		}
	}
	
	private void startCountdown(Random random) {
		startedFrom = (int) (baseCountdownTicks * nextRandomFloat(random));
		countdown = startedFrom;
	}
	
	private float nextRandomFloat(Random random) {
		return 1 + random.nextFloat() - random.nextFloat();
	}
	
	private void generateItemStack() {
		if (!(world instanceof ServerWorld)) {
			return;
		}
		
		ServerWorld serverWorld = (ServerWorld) world;
		MinecraftServer server = serverWorld.getServer();
		
		setItemStack(createRandomItemStack(serverWorld, server));
		playSound(SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 0.5f);
	}
	
	private ItemStack createRandomItemStack(ServerWorld world, MinecraftServer server) {
		LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(resourceLocation);
		LootContext lootContext = getLootContextBuilder(world);
		List<ItemStack> itemStacks = lootTable.generate(lootContext);
		
		return itemStacks.size() > 0 ? itemStacks.get(0) : ItemStack.EMPTY;
	}
	
	protected LootContext getLootContextBuilder(ServerWorld world) {
		return new LootContext.Builder(world)
				.withRandom(world.getRandom())
				.build(LootParameterSets.EMPTY);
	}
	
	public ActionResultType onBlockActivated(@Nonnull PlayerEntity player) {
		
		if (world == null || world.isRemote) {
			return ActionResultType.PASS;
		}
		
		UUID uuid = player.getUniqueID();
		
		if (player.isSneaking()) {
			if (playerUuid == null) {
				activateByPlayer(world, this, uuid);
				sendMessageToPlayer(player, createText("activate.success"));
				return ActionResultType.CONSUME;
			} else if (uuid.equals(playerUuid)) {
				sendMessageToPlayer(player, createText("activate.alreadyActivated"));
				return ActionResultType.FAIL;
			} else {
				sendMessageToPlayer(player, createText("activate.alreadyActivatedByOtherPlayer"));
				return ActionResultType.FAIL;
			}
		}
		
		if (itemStack.isEmpty()) {
			sendMessageToPlayer(player, createText("take.progress", (int) (getProgress() * 100)));
			return ActionResultType.FAIL;
		}
		
		if (playerUuid != null && !uuid.equals(playerUuid)) {
			sendMessageToPlayer(player, createText("take.forbidden"));
			return ActionResultType.FAIL;
		}
		
		MinecraftServer server = ((ServerWorld) world).getServer();
		ITextComponent chatMessage = createText("take.success",
				player.getDisplayName(), itemStack.getTextComponent());
		
		throwItemStack(world, itemStack, player, getPos());
		setItemStack(ItemStack.EMPTY);
		playSound(SoundEvents.ENTITY_ITEM_PICKUP, 0.2f);
		sendMessageToAllPlayers(server, player, chatMessage);
		
		return ActionResultType.CONSUME;
	}
	
	public void onBlockPlacedBy(@Nonnull PlayerEntity player) {
		activateByPlayer(player.world, this, player.getUniqueID());
	}
	
	private ITextComponent createText(String translationKey, Object... args) {
		return new TranslationTextComponent("lootBoxGenerator." + translationKey, args);
	}
	
	private static void sendMessageToPlayer(PlayerEntity player, ITextComponent chatMessage) {
		player.sendMessage(chatMessage, Util.DUMMY_UUID);
	}
	
	private static void sendMessageToAllPlayers(MinecraftServer server, PlayerEntity player, ITextComponent chatMessage) {
		server.getPlayerList().sendMessageToTeamOrAllPlayers(player, chatMessage);
	}
	
	private static void throwItemStack(World world, ItemStack itemStack, @Nonnull PlayerEntity player, BlockPos blockPos) {
		
		double posX = blockPos.getX() + 0.5;
		double posY = blockPos.getY() + 0.75;
		double posZ = blockPos.getZ() + 0.5;
		
		ItemEntity itemEntity = new ItemEntity(world, posX, posY, posZ, itemStack);
		
		double x = player.getPosX() - posX;
		double y = player.getPosYEye() - posY;
		double z = player.getPosZ() - posZ;
		
		itemEntity.setMotion(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1);
		
		world.addEntity(itemEntity);
	}
	
	private void setItemStack(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		setChanged();
	}
	
	private void setChanged() {
		if (world == null) {
			return;
		}
		
		markDirty();
		world.notifyBlockUpdate(getPos(), getBlockState(), getBlockState(), 2);
	}
	
	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public static void activateByPlayer(World world, LootBoxGeneratorTileEntity tileEntity, UUID playerUuid) {
		Map<UUID, BlockPos> activeTileEntities = getActiveTileEntities(world);
		
		if (activeTileEntities.containsKey(playerUuid)) {
			BlockPos blockPos = activeTileEntities.get(playerUuid);
			TileEntity abstractTileEntity = world.getTileEntity(blockPos);
			
			if (abstractTileEntity instanceof LootBoxGeneratorTileEntity) {
				LootBoxGeneratorTileEntity oldTileEntity = (LootBoxGeneratorTileEntity) abstractTileEntity;
				
				if (oldTileEntity == tileEntity) {
					return;
				}
				
				oldTileEntity.isActive = false;
				oldTileEntity.playerUuid = null;
				oldTileEntity.setChanged();
				oldTileEntity.playSound(SoundEvents.BLOCK_BEACON_DEACTIVATE, 1f);
			}
		}
		
		activeTileEntities.put(playerUuid, tileEntity.getPos());
		
		tileEntity.isActive = true;
		tileEntity.playerUuid = playerUuid;
		tileEntity.setChanged();
		tileEntity.playSound(SoundEvents.BLOCK_BEACON_ACTIVATE, 1f);
	}
	
	private static Map<UUID, BlockPos> getActiveTileEntities(World world) {
		if (!activeTileEntitiesByWorlds.containsKey(world)) {
			activeTileEntitiesByWorlds.put(world, new HashMap<>());
		}
		
		return activeTileEntitiesByWorlds.get(world);
	}
	
	private void playSound(SoundEvent sound, float volume) {
		if (world == null) {
			return;
		}
		
		Random random = world.getRandom();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.8F + 1.0F) * 2.0F;
		
		playSound(sound, volume, pitch);
	}
	
	private void playSound(SoundEvent sound, float volume, float pitch) {
		if (world == null) {
			return;
		}
		
		BlockPos blockPos = getPos();
		double posX = blockPos.getX() + 0.5;
		double posY = blockPos.getY() + 0.5;
		double posZ = blockPos.getZ() + 0.5;
		SoundCategory category = SoundCategory.BLOCKS;
		
		world.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
	}
	
	public float getProgress() {
		return 1 - (float) countdown / startedFrom;
	}
	
	public int getAge() {
		return age;
	}
	
	public boolean isActive() {
		return isActive;
	}
	
	@Nullable
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbtTag = new CompoundNBT();
		writeData(nbtTag);
		return new SUpdateTileEntityPacket(getPos(), -1, nbtTag);
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		readData(packet.getNbtCompound());
	}
	
	@Nonnull
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbt = super.getUpdateTag();
		writeData(nbt);
		return nbt;
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
		super.handleUpdateTag(state, nbt);
		readData(nbt);
	}
	
	@Nonnull
	@Override
	public CompoundNBT write(@Nonnull CompoundNBT nbt) {
		super.write(nbt);
		writeData(nbt);
		return nbt;
	}
	
	@Override
	public void read(@Nonnull BlockState state, @Nonnull CompoundNBT nbt) {
		super.read(state, nbt);
		readData(nbt);
	}
	
	private void readData(@Nonnull CompoundNBT nbt) {
		if (nbt.contains("IsActive", Constants.NBT.TAG_BYTE)) {
			isActive = nbt.getBoolean("IsActive");
		}
		
		if (nbt.contains("Countdown", Constants.NBT.TAG_INT)) {
			countdown = nbt.getInt("Countdown");
		}
		
		if (nbt.contains("StartedFrom", Constants.NBT.TAG_INT)) {
			startedFrom = nbt.getInt("StartedFrom");
		}
		
		if (nbt.contains("Player", Constants.NBT.TAG_INT_ARRAY)) {
			playerUuid = nbt.getUniqueId("Player");
		} else {
			playerUuid = null;
		}
		
		if (nbt.contains("ItemStack", Constants.NBT.TAG_COMPOUND)) {
			itemStack = ItemStack.read(nbt.getCompound("ItemStack"));
		} else {
			itemStack = ItemStack.EMPTY;
		}
		
		if (isActive && world != null && !world.isRemote) {
			activateByPlayer(world, this, playerUuid);
		}
	}
	
	private void writeData(@Nonnull CompoundNBT nbt) {
		nbt.putBoolean("IsActive", isActive);
		nbt.putInt("Countdown", countdown);
		nbt.putInt("StartedFrom", startedFrom);
		
		if (playerUuid != null) {
			nbt.putUniqueId("Player", playerUuid);
		}
		
		CompoundNBT itemStackNbt = new CompoundNBT();
		itemStack.write(itemStackNbt);
		nbt.put("ItemStack", itemStackNbt);
	}
}
