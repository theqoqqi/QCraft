package ru.qoqqi.qcraft.blockentities;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.blocks.LootBoxGeneratorBlock;
import ru.qoqqi.qcraft.leveldata.LootBoxGeneratorsLevelData;
import ru.qoqqi.qcraft.particles.ModParticleTypes;

public class LootBoxGeneratorBlockEntity extends BlockEntity implements ItemPedestal {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final ResourceLocation resourceLocation = new ResourceLocation(QCraft.MOD_ID, "random_loot_box");

	private static final int HOUR_IN_TICKS = 60 * 60 * 20;

	private static final float baseCountdownTicks = 3 * HOUR_IN_TICKS;

	private int age;

	private int countdown;

	private int startedFrom;

	public final float hoverStart;

	@Nonnull
	private ItemStack itemStack = ItemStack.EMPTY;

	private boolean isActive;

	private UUID ownerUuid;

	public LootBoxGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
		this(ModBlockEntityTypes.LOOT_BOX_GENERATOR.get(), pos, blockState);
	}

	public LootBoxGeneratorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState blockState) {
		super(blockEntityType, pos, blockState);
		this.hoverStart = (float) (Math.random() * Math.PI * 2.0);
	}

	public void tick() {
		age++;

		if (level == null) {
			return;
		}

		if (level.isClientSide) {
			doClientTick();
		} else {
			doServerTick((ServerLevel) level);
		}
	}

	private void doServerTick(ServerLevel level) {
		if (!isActive || !itemStack.isEmpty()) {
			return;
		}

		countdown--;

		if (countdown <= 0) {
			LootBoxGeneratorsLevelData levelData = getLevelData(level);

			if (startedFrom > 0) {
				generateItemStack();
				LOGGER.info("generated item stack: {}", itemStack);
				levelData.removeGenerationDuration(ownerUuid);
			}

			if (!levelData.hasGenerationDuration(ownerUuid)) {
				levelData.setGenerationDuration(ownerUuid, nextRandomDuration(level.random));
			}

			int duration = levelData.getGenerationDuration(ownerUuid);
			startCountdown(duration);
		}
	}

	private void doClientTick() {
		if (!isActive) {
			return;
		}

		if (countdown > 0) {
			countdown--;
		}

		if (age % 80 == 0) {
			playSound(SoundEvents.BEACON_AMBIENT, 1f);
		}

		if (hasItem()) {
			spawnCircularParticle(0.25f, 0.02f, 0.0275f, 0f);
			spawnCircularParticle(0.25f, 0.02f, 0.0275f, 0.5f);
			spawnCircularParticle(0.4f, 0.0025f, -0.01f, 0f);
		}
	}

	private void spawnCircularParticle(float radius, float ySpeed, float rotationSpeed, float rotationOffset) {
		Vec3 tableCenter = LootBoxGeneratorBlock.TABLE_CENTER;
		BlockPos pos = getBlockPos();
		float angle = (float) (Math.PI * 2 * (age / (1 / rotationSpeed) + rotationOffset));

		double x = pos.getX() + tableCenter.x + Mth.sin(angle) * radius;
		double y = pos.getY() + tableCenter.y;
		double z = pos.getZ() + tableCenter.z + Mth.cos(angle) * radius;

		if (level != null) {
			level.addParticle(ModParticleTypes.LOOT_BOX_GENERATOR.get(), x, y, z, 0, ySpeed, 0);
		}
	}

	private void startCountdown(int startFrom) {
		startedFrom = startFrom;
		countdown = startFrom;
	}

	private int nextRandomDuration(RandomSource random) {
		return (int) (baseCountdownTicks * nextRandomFloat(random));
	}

	private float nextRandomFloat(RandomSource random) {
		return 1 + random.nextFloat() - random.nextFloat();
	}

	private void generateItemStack() {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		MinecraftServer server = serverLevel.getServer();

		setItemStack(createRandomItemStack(serverLevel, server));
		playSound(SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, 0.5f);
	}

	private ItemStack createRandomItemStack(ServerLevel level, MinecraftServer server) {
		LootTable lootTable = server.getLootData().getLootTable(resourceLocation);
		LootParams lootParams = createLootParams(level);
		List<ItemStack> itemStacks = lootTable.getRandomItems(lootParams);

		return !itemStacks.isEmpty() ? itemStacks.get(0) : ItemStack.EMPTY;
	}

	protected LootParams createLootParams(ServerLevel level) {
		return new LootParams.Builder(level)
				.create(LootContextParamSets.EMPTY);
	}

	public InteractionResult onBlockActivated(@Nonnull Player player) {

		if (level == null) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide) {
			return InteractionResult.CONSUME;
		}

		UUID uuid = player.getUUID();

		if (player.isCrouching()) {
			if (ownerUuid == null) {
				activateByPlayer((ServerLevel) level, this, uuid);
				sendMessageToPlayer(player, createText("activate.success"));
				ModCriteriaTriggers.ACTIVATE_LOOT_BOX_GENERATOR.trigger((ServerPlayer) player);
				return InteractionResult.CONSUME;
			} else if (uuid.equals(ownerUuid)) {
				sendMessageToPlayer(player, createText("activate.alreadyActivated"));
				return InteractionResult.CONSUME;
			} else {
				sendMessageToPlayer(player, createText("activate.alreadyActivatedByOtherPlayer"));
				return InteractionResult.CONSUME;
			}
		}

		if (itemStack.isEmpty()) {
			int progress = (int) (getProgress() * 100);
			if (isActive) {
				long millisecondsLeft = countdown / 20 * 1000L;
				String timeText = DurationFormatUtils.formatDuration(millisecondsLeft, "HH:mm:ss");
				sendMessageToPlayer(player, createText("take.progress.active", progress, timeText));
			} else {
				sendMessageToPlayer(player, createText("take.progress.inactive", progress));
			}
			return InteractionResult.CONSUME;
		}

		boolean canTake = ownerUuid == null
				|| uuid.equals(ownerUuid)
				|| player.isCreative();

		if (!canTake) {
			sendMessageToPlayer(player, createText("take.forbidden"));
			return InteractionResult.CONSUME;
		}

		MinecraftServer server = ((ServerLevel) level).getServer();
		Component chatMessage = createText("take.success",
				player.getDisplayName(), itemStack.getDisplayName());

		throwItemStack(level, itemStack, player, getBlockPos());
		setItemStack(ItemStack.EMPTY);
		playSound(SoundEvents.ITEM_PICKUP, 0.2f);
		sendMessageToAllPlayers(server, player, chatMessage);

		return InteractionResult.CONSUME;
	}

	public void onBlockPlacedBy(@Nonnull Player player) {
		activateByPlayer((ServerLevel) player.level(), this, player.getUUID());
		ModCriteriaTriggers.ACTIVATE_LOOT_BOX_GENERATOR.trigger((ServerPlayer) player);
	}

	@Override
	public void setRemoved() {
		if (isActive && level instanceof ServerLevel) {
			removeActiveForPlayer((ServerLevel) level, ownerUuid);
			playSound(SoundEvents.BEACON_DEACTIVATE, 1f);
			if (!itemStack.isEmpty()) {
				throwItemStack(level, itemStack, getBlockPos(), new Vec3(0, 1, 0));
			}
		}
		super.setRemoved();
	}

	private Component createText(String translationKey, Object... args) {
		return Component.translatable("lootBoxGenerator." + translationKey, args);
	}

	private static void sendMessageToPlayer(Player player, Component chatMessage) {
		player.sendSystemMessage(chatMessage);
	}

	private static void sendMessageToAllPlayers(MinecraftServer server, Player player, Component chatMessage) {
		server.getPlayerList().broadcastSystemToAllExceptTeam(player, chatMessage);
	}

	private static void throwItemStack(Level level, ItemStack itemStack, @Nonnull Player player, BlockPos blockPos) {

		double posX = blockPos.getX() + 0.5;
		double posY = blockPos.getY() + 0.75;
		double posZ = blockPos.getZ() + 0.5;

		ItemEntity itemEntity = new ItemEntity(level, posX, posY, posZ, itemStack);

		double x = player.getX() - posX;
		double y = player.getEyeY() - posY;
		double z = player.getZ() - posZ;

		itemEntity.setDeltaMovement(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1);

		level.addFreshEntity(itemEntity);
	}

	private static void throwItemStack(Level level, ItemStack itemStack, BlockPos blockPos, Vec3 motion) {

		double posX = blockPos.getX() + 0.5;
		double posY = blockPos.getY() + 0.75;
		double posZ = blockPos.getZ() + 0.5;

		ItemEntity itemEntity = new ItemEntity(level, posX, posY, posZ, itemStack);

		itemEntity.setDeltaMovement(motion);

		level.addFreshEntity(itemEntity);
	}

	private void setItemStack(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
		setChanged();
	}

	@Override
	public void setChanged() {
		if (level == null) {
			return;
		}

		super.setChanged();
		level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 2);
	}

	@Override
	@Nonnull
	public ItemStack getItemStack() {
		return itemStack;
	}

	@Override
	public boolean hasItem() {
		return !itemStack.isEmpty();
	}

	private static void activateByPlayer(ServerLevel level, LootBoxGeneratorBlockEntity blockEntity, UUID playerUuid) {
		if (isActiveForPlayer(level, playerUuid, blockEntity)) {
			return;
		}

		removeActiveForPlayer(level, playerUuid);
		setActiveForPlayer(level, playerUuid, blockEntity);
	}

	private static boolean isActiveForPlayer(ServerLevel level, UUID playerUuid, LootBoxGeneratorBlockEntity blockEntity) {
		LootBoxGeneratorsLevelData activeBlockEntities = getLevelData(level);

		if (!activeBlockEntities.hasActivatedBlock(playerUuid)) {
			return false;
		}

		BlockPos blockPos = activeBlockEntities.getActivatedBlock(playerUuid);
		BlockEntity abstractBlockEntity = level.getBlockEntity(blockPos);

		if (!(abstractBlockEntity instanceof LootBoxGeneratorBlockEntity)) {
			return false;
		}

		return abstractBlockEntity == blockEntity;
	}

	private static void removeActiveForPlayer(ServerLevel level, UUID playerUuid) {
		LootBoxGeneratorsLevelData levelData = getLevelData(level);

		if (!levelData.hasActivatedBlock(playerUuid)) {
			return;
		}

		BlockPos blockPos = levelData.getActivatedBlock(playerUuid);
		BlockEntity abstractBlockEntity = level.getBlockEntity(blockPos);

		levelData.removeActivatedBlock(playerUuid);

		if (!(abstractBlockEntity instanceof LootBoxGeneratorBlockEntity blockEntity)) {
			return;
		}

		blockEntity.isActive = false;
		blockEntity.ownerUuid = null;
		blockEntity.setChanged();
		blockEntity.playSound(SoundEvents.BEACON_DEACTIVATE, 1f);
	}

	private static void setActiveForPlayer(ServerLevel level, UUID playerUuid, LootBoxGeneratorBlockEntity blockEntity) {
		LootBoxGeneratorsLevelData levelData = getLevelData(level);

		levelData.setActivatedBlock(playerUuid, blockEntity.getBlockPos());

		blockEntity.isActive = true;
		blockEntity.ownerUuid = playerUuid;
		blockEntity.setChanged();
		blockEntity.playSound(SoundEvents.BEACON_ACTIVATE, 1f);
	}

	private static LootBoxGeneratorsLevelData getLevelData(ServerLevel level) {
		return LootBoxGeneratorsLevelData.getInstance(level);
	}

	private void playSound(SoundEvent sound, float volume) {
		if (level == null) {
			return;
		}

		RandomSource random = level.getRandom();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.8F + 1.0F) * 2.0F;

		playSound(sound, volume, pitch);
	}

	private void playSound(SoundEvent sound, float volume, float pitch) {
		if (level == null) {
			return;
		}

		BlockPos blockPos = getBlockPos();
		double posX = blockPos.getX() + 0.5;
		double posY = blockPos.getY() + 0.5;
		double posZ = blockPos.getZ() + 0.5;
		SoundSource category = SoundSource.BLOCKS;
		Player player = level.isClientSide ? Minecraft.getInstance().player : null;

		level.playSound(player, posX, posY, posZ, sound, category, volume, pitch);
	}

	public float getProgress() {
		return 1 - (float) countdown / startedFrom;
	}

	@Override
	public int getAge() {
		return age;
	}

	@Override
	public float getHoverStart() {
		return hoverStart;
	}

	@Override
	public Level getLevel2() {
		return getLevel();
	}

	public boolean isActive() {
		return isActive;
	}

	public UUID getOwnerUuid() {
		return ownerUuid;
	}

	@Nullable
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
		CompoundTag tag = packet.getTag();

		if (tag != null) {
			readData(tag);
		}
	}

	@Nonnull
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag nbt = super.getUpdateTag();
		writeData(nbt);
		return nbt;
	}

	@Override
	public void handleUpdateTag(CompoundTag nbt) {
		super.handleUpdateTag(nbt);
		readData(nbt);
	}

	@Override
	protected void saveAdditional(@NotNull CompoundTag tag) {
		super.saveAdditional(tag);
		writeData(tag);
	}

	@Override
	public void load(@Nonnull CompoundTag nbt) {
		super.load(nbt);
		readData(nbt);
	}

	private void readData(@Nonnull CompoundTag nbt) {
		if (nbt.contains("IsActive", Tag.TAG_BYTE)) {
			isActive = nbt.getBoolean("IsActive");
		}

		if (nbt.contains("Countdown", Tag.TAG_INT)) {
			countdown = nbt.getInt("Countdown");
		}

		if (nbt.contains("StartedFrom", Tag.TAG_INT)) {
			startedFrom = nbt.getInt("StartedFrom");
		}

		if (nbt.contains("OwnerUuid", Tag.TAG_INT_ARRAY)) {
			ownerUuid = nbt.getUUID("OwnerUuid");
		} else {
			ownerUuid = null;
		}

		if (nbt.contains("ItemStack", Tag.TAG_COMPOUND)) {
			itemStack = ItemStack.of(nbt.getCompound("ItemStack"));
		} else {
			itemStack = ItemStack.EMPTY;
		}
		LOGGER.info("readData: {}", nbt);
	}

	private void writeData(@Nonnull CompoundTag nbt) {
		nbt.putBoolean("IsActive", isActive);
		nbt.putInt("Countdown", countdown);
		nbt.putInt("StartedFrom", startedFrom);

		if (ownerUuid != null) {
			nbt.putUUID("OwnerUuid", ownerUuid);
		}

		nbt.put("ItemStack", itemStack.save(new CompoundTag()));
	}
}
