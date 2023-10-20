package ru.qoqqi.qcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.boxes.LootBoxes;
import ru.qoqqi.qcraft.particles.JellyBlobPieceParticleOption;
import ru.qoqqi.qcraft.particles.ModParticleTypes;
import ru.qoqqi.qcraft.sounds.ModSoundEvents;

public class JellyBlob extends Mob {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final float MODEL_SCALE = 2.9f;

	public static final float BLOWING_UP_MODEL_SCALE = 4.0f;

	private static final int BLOW_UP_DURATION_IN_TICKS = 60;

	private static final EntityDataAccessor<String> DATA_TYPE_ID =
			SynchedEntityData.defineId(JellyBlob.class, EntityDataSerializers.STRING);

	private static final EntityDataAccessor<Integer> DATA_FOOD_LEFT_TO_BLOW_UP_ID =
			SynchedEntityData.defineId(JellyBlob.class, EntityDataSerializers.INT);

	private static final EntityDataAccessor<Integer> DATA_FOOD_GAINED_AT_TICK_ID =
			SynchedEntityData.defineId(JellyBlob.class, EntityDataSerializers.INT);

	private UUID lastFoodGivenBy;

	public JellyBlob(EntityType<? extends JellyBlob> entityType, Level level) {
		super(entityType, level);

		setCanPickUpLoot(true);
	}

	protected void registerGoals() {

	}

	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TYPE_ID, "");
		this.entityData.define(DATA_FOOD_LEFT_TO_BLOW_UP_ID, 0);
		this.entityData.define(DATA_FOOD_GAINED_AT_TICK_ID, 0);
	}

	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);

		if (tag.contains("BlobType", Tag.TAG_STRING)) {
			setBlobTypeName(tag.getString("BlobType"));
		}

		if (tag.contains("FoodLeftToBlowUp", Tag.TAG_INT)) {
			setFoodLeftToBlowUp(tag.getInt("FoodLeftToBlowUp"));
		}

		if (tag.contains("TicksElapsedAfterFoodGained", Tag.TAG_INT)) {
			setFoodGainedAtTick(tickCount - tag.getInt("TicksElapsedAfterFoodGained"));
		}

		if (tag.hasUUID("LastFoodGivenBy")) {
			setLastFoodGivenBy(tag.getUUID("LastFoodGivenBy"));
		}
	}

	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		tag.putString("BlobType", getBlobTypeName());
		tag.putInt("FoodLeftToBlowUp", getFoodLeftToBlowUp());

		if (isFoodGained()) {
			tag.putInt("TicksElapsedAfterFoodGained", tickCount - getFoodGainedAtTick());
		}

		if (getLastFoodGivenBy() != null) {
			tag.putUUID("LastFoodGivenBy", lastFoodGivenBy);
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (isAlive() && readyToBlowUp()) {
			blowUp();
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();

		customPickUp();
	}

	/**
	 * Этот метод копирует поведение подбирания предметов из {@link Mob#aiStep()},
	 * игнорируя задержку перед подбором предмета ({@link ItemEntity#hasPickUpDelay()}).
	 */
	public void customPickUp() {
		var pickupReach = this.getPickupReach();

		boolean canPickUpItems = !level().isClientSide
				&& canPickUpLoot()
				&& isAlive()
				&& !dead
				&& ForgeEventFactory.getMobGriefingEvent(level(), this);

		if (!canPickUpItems) {
			return;
		}

		var searchArea = getBoundingBox()
				.inflate(pickupReach.getX(), pickupReach.getY(), pickupReach.getZ());
		var nearbyEntities = level().getEntitiesOfClass(ItemEntity.class, searchArea);

		for (var itemEntity : nearbyEntities) {
			var canPuckUp = !itemEntity.isRemoved()
					&& !itemEntity.getItem().isEmpty()
					// Игнорируем эту задержку, чтобы блоб поглощал еду моментально
					// && !itemEntity.hasPickUpDelay()
					&& wantsToPickUp(itemEntity.getItem());

			if (canPuckUp) {
				pickUpItem(itemEntity);
			}
		}
	}

	@Override
	public boolean wantsToPickUp(@NotNull ItemStack itemStack) {
		return super.wantsToPickUp(itemStack)
				&& !isFoodGained()
				&& isEatableItem(itemStack);
	}

	@Override
	protected void pickUpItem(@NotNull ItemEntity itemEntity) {
		var itemStack = itemEntity.getItem();
		var countToTake = Math.min(getFoodLeftToBlowUp(), itemStack.getCount());

		if (countToTake <= 0) {
			return;
		}

		var playerUuid = Optional.ofNullable(itemEntity.getOwner())
				.filter(ServerPlayer.class::isInstance)
				.map(ServerPlayer.class::cast)
				.map(ServerPlayer::getUUID)
				.orElse(null);

		onItemPickup(itemEntity);
		take(itemEntity, countToTake);
		itemStack.shrink(countToTake);

		if (itemStack.isEmpty()) {
			itemEntity.discard();
		}

		feed(countToTake, playerUuid);
	}

	private void feed(int foodCount, UUID playerUuid) {
		int foodLeftToBlowUp = getFoodLeftToBlowUp();
		int newFoodLeftToBlowUp = foodLeftToBlowUp - foodCount;

		setFoodLeftToBlowUp(newFoodLeftToBlowUp);

		if (playerUuid != null) {
			setLastFoodGivenBy(playerUuid);
		}

		if (newFoodLeftToBlowUp <= 0) {
			setFoodGainedAtTick(tickCount);
			playSound(ModSoundEvents.JELLY_BLOB_INFLATE.get());
		}
	}

	private void blowUp() {
		if (level().isClientSide) {
			return;
		}

		kill();
		applyBlowUpLootBox();
		dropBlowUpLoot();
		spawnBlowUpParticles();
		playSound(ModSoundEvents.JELLY_BLOB_BLOW_UP.get());
	}

	private void spawnBlowUpParticles() {
		var serverLevel = (ServerLevel) level();
		var position = position();
		var spread = JellyBlob.BLOWING_UP_MODEL_SCALE / 2;
		var particleOptions = new JellyBlobPieceParticleOption(ModParticleTypes.JELLY_BLOB.get(), getBlobTypeName());

		for (int i = 0; i < 100; i++) {
			spawnBlowUpParticle(particleOptions, serverLevel, position, random, spread, 0.5f);
		}

		spawnBlowUpParticle(ParticleTypes.EXPLOSION_EMITTER, serverLevel, position, random, 0, 0.2f);
	}

	private void spawnBlowUpParticle(ParticleOptions options, @Nonnull ServerLevel level, @Nonnull Vec3 pos, @Nonnull RandomSource random, float spread, float maxSpeed) {
		var x = pos.x + (random.nextDouble() - random.nextDouble()) * spread;
		var y = pos.y + (random.nextDouble() + random.nextDouble()) * spread;
		var z = pos.z + (random.nextDouble() - random.nextDouble()) * spread;
		var xSpeed = random.nextGaussian() * maxSpeed;
		var ySpeed = (random.nextGaussian() + maxSpeed) * maxSpeed;
		var zSpeed = random.nextGaussian() * maxSpeed;

		level.sendParticles(options, x, y, z, 1, xSpeed, ySpeed, zSpeed, maxSpeed);
	}

	private void applyBlowUpLootBox() {
		var lootBox = getBlobType().lootBox;

		if (lootBox == null) {
			return;
		}

		var player = level().getPlayerByUUID(getLastFoodGivenBy());

		if (player == null) {
			player = level().getNearestPlayer(this, -1);
		}

		lootBox.openSilently(player, blockPosition());
	}

	private void dropBlowUpLoot() {
		dropBlowUpLootTable();
		dropBlowUpExperience();
	}

	private void dropBlowUpLootTable() {
		var server = level().getServer();

		if (server == null) {
			return;
		}

		var lootTablePath = "entities/jelly_blob/" + getBlobType().internalName;
		var resourceLocation = new ResourceLocation(QCraft.MOD_ID, lootTablePath);
		var lootTable = server.getLootData().getLootTable(resourceLocation);
		var lootParams = createLootParams((ServerLevel) level());

		lootTable.getRandomItems(lootParams).forEach(this::spawnAtLocation);
	}

	protected LootParams createLootParams(ServerLevel level) {
		return new LootParams.Builder(level)
				.create(LootContextParamSets.EMPTY);
	}

	private void dropBlowUpExperience() {
		ExperienceOrb.award((ServerLevel) level(), position(), getBlobType().experience);
	}

	private boolean isEatableItem(ItemStack itemStack) {
		return getBlobType().foodPredicate.test(itemStack);
	}

	public static <T extends Mob> boolean checkJellyBlobSpawnRules(
			@SuppressWarnings("unused") EntityType<T> entityType,
			ServerLevelAccessor levelAccessor,
			MobSpawnType spawnType,
			BlockPos blockPos,
			@SuppressWarnings("unused") RandomSource random
	) {
		var level = levelAccessor.getLevel();
		var allowedTypes = getJellyBlobTypes(level, blockPos);
		var biome = level.getBiome(blockPos);

		if (allowedTypes.findAny().isEmpty()) {
			return false;
		}

		if (!biome.is(Tags.Biomes.IS_WATER) && spawnType != MobSpawnType.CHUNK_GENERATION) {
			return blockPos.getY() < level.getSeaLevel();
		}

		if (biome.is(BiomeTags.IS_OCEAN)) {
			return random.nextFloat() < 0.1f;
		}

		if (biome.is(BiomeTags.IS_NETHER)) {
			return random.nextFloat() < 0.25f;
		}

		return true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 50.0)
				.add(Attributes.MOVEMENT_SPEED, 0.0)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1);
	}

	@SuppressWarnings("deprecation")
	@Override
	public SpawnGroupData finalizeSpawn(
			@NotNull ServerLevelAccessor levelAccessor,
			@NotNull DifficultyInstance difficulty,
			@NotNull MobSpawnType mobSpawnType,
			@Nullable SpawnGroupData spawnData,
			@Nullable CompoundTag tag
	) {
		var blockPos = blockPosition();
		var level = levelAccessor.getLevel();
		var blobType = getRandomJellyBlobType(level, blockPos);

		setupBlobType(blobType);

		LOGGER.info(
				"Spawned Jelly Blob with type: \"{}\" at ({}, {}, selected from: {})",
				blobType.internalName,
				blockPos.toShortString(),
				level.getBiome(blockPos).unwrapKey()
						.map(k -> k.location().toString())
						.orElse("null"),
				getJellyBlobTypes(level, blockPos)
						.map(t -> t.internalName)
						.collect(Collectors.joining(", "))
		);

		//noinspection OverrideOnly
		return super.finalizeSpawn(levelAccessor, difficulty, mobSpawnType, spawnData, tag);
	}

	private static JellyBlobType getRandomJellyBlobType(ServerLevel level, BlockPos blockPos) {
		var blobTypes = getJellyBlobTypes(level, blockPos).toList();

		if (blobTypes.isEmpty()) {
			return JellyBlobType.getRandom();
		}

		var randomIndex = level.random.nextInt(blobTypes.size());

		return blobTypes.get(randomIndex);
	}

	private static Stream<JellyBlobType> getJellyBlobTypes(ServerLevel level, BlockPos blockPos) {
		var isUnique = level.random.nextFloat() < JellyBlobType.UNIQUE_TYPE_CHANCE;
		var biome = level.getBiome(blockPos);
		var types = isUnique
				? JellyBlobType.getUniqueTypes()
				: JellyBlobType.getCommonTypes();

		return types
				.filter(type -> type.biomePredicate.test(biome))
				.filter(type -> type.locationPredicate.test(blockPos, level));
	}

	public boolean isFoodGained() {
		return getFoodLeftToBlowUp() == 0;
	}

	public boolean readyToBlowUp() {
		var foodGainedAtTick = getFoodGainedAtTick();

		if (foodGainedAtTick == 0) {
			return false;
		}

		return tickCount >= foodGainedAtTick + BLOW_UP_DURATION_IN_TICKS;
	}

	public float getBlowUpProgress(float ageInTicks) {
		var foodGainedAtTick = getFoodGainedAtTick();

		if (foodGainedAtTick == 0) {
			return 0;
		}

		var progress = (ageInTicks - foodGainedAtTick) / BLOW_UP_DURATION_IN_TICKS;

		return Math.min(progress, 1f);
	}

	public JellyBlobType getBlobType() {
		var blobTypeName = getBlobTypeName();
		var blobType = JellyBlobType.get(blobTypeName);

		if (blobType == null) {
			blobType = JellyBlobType.get("plains");
			LOGGER.error("Blob type \"{}\" not found. Falling back to \"plains\".", blobTypeName);
		}

		return blobType;
	}

	private void setupBlobType(JellyBlobType blobType) {
		setBlobTypeName(blobType.internalName);
		setFoodLeftToBlowUp(blobType.foodCount);
	}

	public String getBlobTypeName() {
		return this.entityData.get(DATA_TYPE_ID);
	}

	private void setBlobTypeName(String blobTypeName) {
		this.entityData.set(DATA_TYPE_ID, blobTypeName);
	}

	public int getFoodLeftToBlowUp() {
		return this.entityData.get(DATA_FOOD_LEFT_TO_BLOW_UP_ID);
	}

	private void setFoodLeftToBlowUp(int foodLeftToBlowUp) {
		this.entityData.set(DATA_FOOD_LEFT_TO_BLOW_UP_ID, foodLeftToBlowUp);
	}

	public int getFoodGainedAtTick() {
		return this.entityData.get(DATA_FOOD_GAINED_AT_TICK_ID);
	}

	private void setFoodGainedAtTick(int foodGainedAtTick) {
		this.entityData.set(DATA_FOOD_GAINED_AT_TICK_ID, foodGainedAtTick);
	}

	public UUID getLastFoodGivenBy() {
		return lastFoodGivenBy;
	}

	private void setLastFoodGivenBy(UUID lastFoodGivenBy) {
		this.lastFoodGivenBy = lastFoodGivenBy;
	}

	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean canBeLeashed(@NotNull Player player) {
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean canBreatheUnderwater() {
		return true;
	}

	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
		return SoundEvents.SLIME_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.SLIME_DEATH;
	}

	protected float getSoundVolume() {
		return 0.5F;
	}

	protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions size) {
		return size.height * 0.8f;
	}

	public static class JellyBlobType {

		private static final Map<String, JellyBlobType> BY_NAMES = new HashMap<>();

		private static final Random random = new Random();

		public static final float UNIQUE_TYPE_CHANCE = 0.05f;

		public final String internalName;

		public final Predicate<Holder<Biome>> biomePredicate;

		public final BiPredicate<BlockPos, ServerLevel> locationPredicate;

		public final Predicate<ItemStack> foodPredicate;

		public final int foodCount;

		public final int experience;

		public final LootBox lootBox;

		public final boolean isUnique;

		private final ToIntFunction<JellyBlob> colorGetter;

		static {
			// Помимо нового типа в этом файле должны быть добавлены:
			// Текстуры (опционально)
			// Таблица лута
			// Спаун

			Builder.create()
					.setInternalName("plains")
					.setBiomes(Tags.Biomes.IS_PLAINS)
					.setFood(30, Items.WHEAT)
					.setExperience(100)
					.setColor(166, 149, 83)
					.setLootBox(LootBoxes.PLAINS_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("forest")
					.setBiomes(Biomes.FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST)
					.setFood(30, Items.APPLE)
					.setExperience(100)
					.setColor(68, 99, 26)
					.setLootBox(LootBoxes.FOREST_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("desert")
					.setBiomes(Tags.Biomes.IS_DESERT)
					.setFood(30, Items.CACTUS)
					.setExperience(100)
					.setColor(198, 174, 113)
					.setLootBox(LootBoxes.DESERT_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("beach")
					.setBiomes(Biomes.BEACH)
					.setFood(30, Items.SUGAR_CANE)
					.setExperience(100)
					.setColor(170, 219, 116)
					.setLootBox(LootBoxes.BEACH_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("snowy")
					.setBiomes(Tags.Biomes.IS_SNOWY)
					.setFood(30, Items.SWEET_BERRIES)
					.setExperience(100)
					.setColor(255, 255, 255)
					.setLootBox(LootBoxes.SNOWY_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("swamp")
					.setBiomes(Tags.Biomes.IS_SWAMP)
					.setFood(30, Items.SLIME_BALL)
					.setExperience(100)
					.setColor(54, 73, 27)
					.setLootBox(LootBoxes.SWAMP_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("jungle")
					.setBiomes(BiomeTags.IS_JUNGLE)
					.setFood(30, Items.COCOA_BEANS)
					.setExperience(100)
					.setColor(76, 43, 19)
					.setLootBox(LootBoxes.JUNGLE_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("savanna")
					.setBiomes(BiomeTags.IS_SAVANNA)
					.setFood(30, Items.MELON_SLICE)
					.setExperience(100)
					.setColor(132, 137, 32)
					.setLootBox(LootBoxes.SAVANNA_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("badlands")
					.setBiomes(BiomeTags.IS_BADLANDS)
					.setFood(30, Items.GOLD_INGOT)
					.setExperience(200)
					.setColor(255, 216, 62)
					.setLootBox(LootBoxes.BADLANDS_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("honey")
					.setBiomes(Biomes.FLOWER_FOREST, Biomes.CHERRY_GROVE)
					.setFood(30, Items.HONEYCOMB)
					.setExperience(100)
					.setColor(234, 142, 22)
					.setLootBox(LootBoxes.HONEY_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("mushroom")
					.setBiomes(Tags.Biomes.IS_MUSHROOM)
					.setFood(30, Items.BROWN_MUSHROOM, Items.RED_MUSHROOM)
					.setExperience(100)
					.setColor(226, 18, 18)
					.setLootBox(LootBoxes.MUSHROOM_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("river")
					.setBiomes(BiomeTags.IS_RIVER)
					.setFood(30, ItemTags.FISHES)
					.setExperience(200)
					.setColor(93, 180, 255)
					.setLootBox(LootBoxes.RIVER_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("ocean")
					.setBiomes(BiomeTags.IS_OCEAN)
					.setFood(30, Items.KELP)
					.setExperience(200)
					.setColor(82, 87, 255)
					.setLootBox(LootBoxes.OCEAN_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("underground")
					.setBiomes(BiomeTags.IS_OVERWORLD)
					.excludeBiomes(Tags.Biomes.IS_CAVE, BiomeTags.IS_RIVER, BiomeTags.IS_OCEAN)
					.setLocationPredicate((blockPos, serverLevel) -> {
						var heightmapType = Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
						var x = blockPos.getX();
						var y = blockPos.getY();
						var z = blockPos.getZ();

						return y >= 0 && y < serverLevel.getHeight(heightmapType, x, z);
					})
					.setFood(30, Items.ROTTEN_FLESH)
					.setExperience(100)
					.setColor(127, 127, 127)
					.setLootBox(LootBoxes.UNDERGROUND_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("deep_underground")
					.setBiomes(BiomeTags.IS_OVERWORLD)
					.excludeBiomes(Tags.Biomes.IS_CAVE, BiomeTags.IS_RIVER, BiomeTags.IS_OCEAN)
					.setLocationOptions(-64, 0)
					.setFood(30, Items.COAL)
					.setExperience(100)
					.setColor(47, 47, 55)
					.setLootBox(LootBoxes.DEEP_UNDERGROUND_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("dripstone_caves")
					.setBiomes(Biomes.DRIPSTONE_CAVES)
					.setFood(30, Items.POINTED_DRIPSTONE)
					.setExperience(100)
					.setColor(131, 99, 86)
					.setLootBox(LootBoxes.DRIPSTONE_CAVES_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("lush_caves")
					.setBiomes(Biomes.LUSH_CAVES)
					.setFood(30, Items.GLOW_BERRIES)
					.setExperience(200)
					.setColor(241, 150, 69)
					.setLootBox(LootBoxes.LUSH_CAVES_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("deep_dark")
					.setBiomes(Biomes.DEEP_DARK)
					.setFood(30, Items.DIAMOND)
					.setExperience(300)
					.setColor(3, 65, 80)
					.setLootBox(LootBoxes.DEEP_DARK_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("nether")
					.setBiomes(BiomeTags.IS_NETHER)
					.setLocationOptions(-64, 320)
					.setFood(30, Items.WARPED_FUNGUS, Items.CRIMSON_FUNGUS)
					.setExperience(200)
					.setColor(230, 100, 16)
					.setLootBox(LootBoxes.NETHER_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("end_pearls")
					.setBiomes(Biomes.SMALL_END_ISLANDS, Biomes.END_MIDLANDS)
					.setLocationOptions(-64, 320)
					.setFood(30, Items.ENDER_PEARL)
					.setExperience(300)
					.setColor(32, 32, 32)
					.setLootBox(LootBoxes.END_PEARLS_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("end_chorus")
					.setBiomes(Biomes.END_MIDLANDS, Biomes.END_HIGHLANDS)
					.setLocationOptions(-64, 320)
					.setFood(30, Items.CHORUS_FRUIT)
					.setExperience(300)
					.setColor(55, 39, 71)
					.setLootBox(LootBoxes.END_CHORUS_JELLY_BLOB_LOOT_BOX)
					.build();

			Builder.create()
					.setInternalName("rainbow")
					.setBiomes(BiomeTags.IS_OVERWORLD)
					.setLocationOptions(-64, 320)
					.setFood(1, Items.CAKE)
					.setExperience(0)
					.setColorGetter(jellyBlob -> {
						float hue;
						var saturation = 1f;

						if (jellyBlob == null) {
							hue = random.nextFloat();
						} else {
							var secondsPerCycle = jellyBlob.isFoodGained() ? 2f : 18f;
							var ticksPerCycle = 20f * secondsPerCycle;
							var tickCount = jellyBlob.tickCount;

							hue = (tickCount / ticksPerCycle) % 1;

							if (jellyBlob.isFoodGained()) {
								var blowUpProgress = jellyBlob.getBlowUpProgress(tickCount);

								saturation = 1f - blowUpProgress * 0.8f;
							}
						}

						return Mth.hsvToRgb(hue, saturation, 1f);
					})
					.setLootBox(LootBoxes.RAINBOW_JELLY_BLOB_LOOT_BOX)
					.setUnique()
					.build();
		}

		public JellyBlobType(
				String internalName,
				Predicate<Holder<Biome>> biomePredicate,
				BiPredicate<BlockPos, ServerLevel> locationPredicate,
				Predicate<ItemStack> foodPredicate,
				int foodCount,
				int experience,
				LootBox lootBox,
				ToIntFunction<JellyBlob> colorGetter,
				boolean isUnique
		) {

			if (BY_NAMES.containsKey(internalName)) {
				var message = "JellyBlobType with name \"" + internalName + "\" already exits";

				throw new IllegalArgumentException(message);
			}

			this.internalName = internalName;
			this.biomePredicate = biomePredicate;
			this.locationPredicate = locationPredicate;
			this.foodPredicate = foodPredicate;
			this.foodCount = foodCount;
			this.experience = experience;
			this.lootBox = lootBox;
			this.colorGetter = colorGetter;
			this.isUnique = isUnique;

			BY_NAMES.put(internalName, this);
		}

		public static JellyBlobType get(String internalName) {
			return BY_NAMES.get(internalName);
		}

		public float getRed(JellyBlob jellyBlob) {
			return FastColor.ARGB32.red(colorGetter.applyAsInt(jellyBlob)) / 255f;
		}

		public float getGreen(JellyBlob jellyBlob) {
			return FastColor.ARGB32.green(colorGetter.applyAsInt(jellyBlob)) / 255f;
		}

		public float getBlue(JellyBlob jellyBlob) {
			return FastColor.ARGB32.blue(colorGetter.applyAsInt(jellyBlob)) / 255f;
		}

		private static JellyBlobType getRandom() {
			var values = BY_NAMES.values().toArray(new JellyBlobType[0]);
			var randomIndex = random.nextInt(values.length);

			return values[randomIndex];
		}

		private static Stream<JellyBlobType> getCommonTypes() {
			return BY_NAMES.values().stream().filter(type -> !type.isUnique);
		}

		private static Stream<JellyBlobType> getUniqueTypes() {
			return BY_NAMES.values().stream().filter(type -> type.isUnique);
		}

		public static class Builder {

			private String internalName;
			private Predicate<Holder<Biome>> biomePredicate = biomeHolder -> true;
			private BiPredicate<BlockPos, ServerLevel> locationPredicate = (blockPos, level)
					-> blockPos.getY() >= level.getSeaLevel();
			private Predicate<ItemStack> foodPredicate;
			private int foodCount;
			private int experience;
			private LootBox lootBox;
			private ToIntFunction<JellyBlob> colorGetter;
			private boolean isUnique;

			public static Builder create() {
				return new Builder();
			}

			public Builder setInternalName(String internalName) {
				this.internalName = internalName;
				return this;
			}

			@SafeVarargs
			public final Builder setBiomes(TagKey<Biome>... biomes) {
				this.biomePredicate = this.biomePredicate
						.and(biome -> Arrays.stream(biomes).anyMatch(biome::is));
				return this;
			}

			@SafeVarargs
			public final Builder setBiomes(ResourceKey<Biome>... biomes) {
				this.biomePredicate = this.biomePredicate
						.and(biome -> Arrays.stream(biomes).anyMatch(biome::is));
				return this;
			}

			@SafeVarargs
			public final Builder excludeBiomes(TagKey<Biome>... biomes) {
				this.biomePredicate = this.biomePredicate
						.and(biome -> Arrays.stream(biomes).noneMatch(biome::is));
				return this;
			}

			public Builder setLocationOptions(int min, int max) {
				this.locationPredicate = (blockPos, level) -> {
					var y = blockPos.getY();

					return y >= min && y <= max;
				};
				return this;
			}

			public Builder setLocationPredicate(BiPredicate<BlockPos, ServerLevel> predicate) {
				this.locationPredicate = predicate;
				return this;
			}

			@SafeVarargs
			public final Builder setFood(int foodCount, TagKey<Item>... foodItems) {
				this.foodPredicate = itemStack -> Arrays.stream(foodItems).anyMatch(itemStack::is);
				this.foodCount = foodCount;
				return this;
			}

			public Builder setFood(int foodCount, Item... foodItems) {
				this.foodPredicate = itemStack -> Arrays.stream(foodItems).anyMatch(itemStack::is);
				this.foodCount = foodCount;
				return this;
			}

			public Builder setExperience(int experience) {
				this.experience = experience;
				return this;
			}

			public Builder setLootBox(LootBox lootBox) {
				this.lootBox = lootBox;
				return this;
			}

			public Builder setColor(int red, int green, int blue) {
				this.colorGetter = jellyBlob -> FastColor.ARGB32.color(0, red, green, blue);
				return this;
			}

			public Builder setColorGetter(ToIntFunction<JellyBlob> colorGetter) {
				this.colorGetter = colorGetter;
				return this;
			}

			public Builder setUnique() {
				this.isUnique = true;
				return this;
			}

			public void build() {
				new JellyBlobType(
						internalName,
						biomePredicate,
						locationPredicate,
						foodPredicate,
						foodCount,
						experience,
						lootBox,
						colorGetter,
						isUnique
				);
			}
		}
	}
}
