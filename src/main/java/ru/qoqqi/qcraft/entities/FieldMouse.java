package ru.qoqqi.qcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import ru.qoqqi.qcraft.entities.ai.CustomRemoveBlockGoal;
import ru.qoqqi.qcraft.util.IntRange;

public class FieldMouse extends Animal {
	
	public static final int TYPE_PLAINS = 0;
	
	public static final int TYPE_FOREST = 1;
	
	public static final int TYPE_SAVANNA = 2;
	
	public static final int TYPE_DESERT = 3;
	
	public static final int TYPE_SWAMP = 4;
	
	public static final int TYPE_SNOWY = 5;
	
	private static final EntityDataAccessor<Integer> DATA_TYPE_ID =
			SynchedEntityData.defineId(FieldMouse.class, EntityDataSerializers.INT);
	
	private static final Ingredient FOOD_ITEMS = Ingredient.of(Tags.Items.SEEDS);
	
	private static final Set<TagKey<Item>> ALLOWED_ITEMS = Set.of(Tags.Items.SEEDS);
	
	private static final Predicate<BlockState> blocksToDestroy =
			blockState -> blockState.is(Blocks.BEETROOTS)
					|| blockState.is(Blocks.MELON_STEM)
					|| blockState.is(Blocks.PUMPKIN_STEM)
					|| blockState.is(Blocks.WHEAT);
	
	private static final Predicate<BlockState> grassBlocksToDestroy =
			blockState -> blockState.is(Blocks.GRASS);
	
	private static final Predicate<BlockState> desertBlocksToDestroy =
			blockState -> blockState.is(Blocks.DEAD_BUSH);
	
	private static final IntRange HUNGER_COOLDOWN_RANGE = IntRange.of(1200, 2400);
	
	private int hungerCooldown;
	
	protected FieldMouse(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
		
		setCanPickUpLoot(true);
		scheduleNextHunger();
	}
	
	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Ocelot.class, 6.0f, 1.0, 1.2));
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Cat.class, 6.0f, 1.0, 1.2));
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 6.0f, 1.0, 1.2));
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Villager.class, 6.0f, 1.0, 1.2));
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(4, new TemptGoal(this, 1.25, FOOD_ITEMS, true));
		this.goalSelector.addGoal(5, new MouseCollectSeedsGoal(this, FieldMouse::isCollectableItem));
		this.goalSelector.addGoal(6, new MouseRemoveBlockGoal(this, blocksToDestroy, 32));
		this.goalSelector.addGoal(6, new MouseRemoveBlockGoal(this, grassBlocksToDestroy, 8));
		this.goalSelector.addGoal(6, new MouseRemoveBlockGoal(this, desertBlocksToDestroy, 16, true));
		this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.25D));
		this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0f));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
	}
	
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TYPE_ID, 0);
	}
	
	@Override
	public SpawnGroupData finalizeSpawn(
			@NotNull ServerLevelAccessor levelAccessor,
			@NotNull DifficultyInstance difficulty,
			@NotNull MobSpawnType mobSpawnType,
			@Nullable SpawnGroupData spawnData,
			@Nullable CompoundTag tag
	) {
		var mouseTypeId = getMouseTypeForBiome(levelAccessor.getBiome(blockPosition()));
		
		setMouseType(mouseTypeId);
		
		return super.finalizeSpawn(levelAccessor, difficulty, mobSpawnType, spawnData, tag);
	}
	
	private int getMouseTypeForBiome(Holder<Biome> biome) {
		if (biome.is(Tags.Biomes.IS_PLAINS)) {
			return TYPE_PLAINS;
		}
		
		if (biome.is(BiomeTags.IS_FOREST)) {
			return TYPE_FOREST;
		}
		
		if (biome.is(BiomeTags.IS_SAVANNA)) {
			return TYPE_SAVANNA;
		}
		
		if (biome.is(Tags.Biomes.IS_DESERT)) {
			return TYPE_DESERT;
		}
		
		if (biome.is(Tags.Biomes.IS_SWAMP)) {
			return TYPE_SWAMP;
		}
		
		if (biome.is(Tags.Biomes.IS_SNOWY)) {
			return TYPE_SNOWY;
		}
		
		return TYPE_PLAINS;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		hungerCooldown--;
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isHungry() {
		return hungerCooldown <= 0;
	}
	
	private void scheduleNextHunger() {
		hungerCooldown = HUNGER_COOLDOWN_RANGE.getRandomValue(random);
	}
	
	@Override
	public boolean wantsToPickUp(@NotNull ItemStack itemStack) {
		return super.wantsToPickUp(itemStack)
				&& isCollectableItem(itemStack);
	}
	
	@Override
	protected void pickUpItem(@NotNull ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		
		onItemPickup(itemEntity);
		take(itemEntity, itemStack.getCount());
		
		itemEntity.discard();
		
		scheduleNextHunger();
	}
	
	protected SoundEvent getAmbientSound() {
		return SoundEvents.BAT_AMBIENT;
	}
	
	protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
		return SoundEvents.BAT_HURT;
	}
	
	protected SoundEvent getDeathSound() {
		return SoundEvents.BAT_DEATH;
	}
	
	protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState block) {
		this.playSound(SoundEvents.AZALEA_STEP, 0.05F, 1.0F);
	}
	
	protected float getSoundVolume() {
		return 0.5F;
	}
	
	public FieldMouse getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
		var mouse = ModEntityTypes.FIELD_MOUSE.get().create(level);
		
		if (mouse != null) {
			int mouseTypeId = getMouseType();
			
			if (otherParent instanceof FieldMouse otherMouse && random.nextBoolean()) {
				mouseTypeId = otherMouse.getMouseType();
			}
			
			mouse.setMouseType(mouseTypeId);
		}
		
		return mouse;
	}
	
	public boolean isFood(@NotNull ItemStack itemStack) {
		return FOOD_ITEMS.test(itemStack);
	}
	
	protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions size) {
		return 0.125f;
	}
	
	public int getMouseType() {
		return this.entityData.get(DATA_TYPE_ID);
	}
	
	private void setMouseType(int mouseTypeId) {
		this.entityData.set(DATA_TYPE_ID, mouseTypeId);
	}
	
	@Override
	public void readAdditionalSaveData(@NotNull CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		
		if (tag.contains("HungerCooldown", Tag.TAG_INT)) {
			hungerCooldown = tag.getInt("HungerCooldown");
		}
		
		if (tag.contains("MouseType", Tag.TAG_INT)) {
			setMouseType(tag.getInt("MouseType"));
		}
	}
	
	@Override
	public void addAdditionalSaveData(@NotNull CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		
		tag.putInt("HungerCooldown", hungerCooldown);
		tag.putInt("MouseType", getMouseType());
	}
	
	private static boolean isCollectableItem(ItemEntity itemEntity) {
		return !itemEntity.hasPickUpDelay()
				&& itemEntity.isAlive()
				&& ALLOWED_ITEMS.stream().anyMatch(itemEntity.getItem()::is);
	}
	
	private static boolean isCollectableItem(ItemStack itemStack) {
		return ALLOWED_ITEMS.stream().anyMatch(itemStack::is);
	}
	
	public static boolean checkFieldMouseSpawnRules(
			@SuppressWarnings("unused") EntityType<FieldMouse> entityType,
			ServerLevelAccessor levelAccessor,
			@SuppressWarnings("unused") MobSpawnType mobSpawnType,
			BlockPos blockPos,
			@SuppressWarnings("unused") RandomSource randomSource
	) {
		var canSpawnOn = new Block[] { Blocks.GRASS_BLOCK, Blocks.SAND };
		var blockState = levelAccessor.getBlockState(blockPos.below());
		
		return Arrays.stream(canSpawnOn).anyMatch(blockState::is)
				&& isBrightEnoughToSpawn(levelAccessor, blockPos);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3f);
	}
	
	private static class MouseRemoveBlockGoal extends CustomRemoveBlockGoal {
		
		private final FieldMouse mouse;
		
		private final boolean satisfiesHunger;
		
		public MouseRemoveBlockGoal(FieldMouse mouse, Predicate<BlockState> blockPredicate, int searchDistance) {
			this(mouse, blockPredicate, searchDistance, false);
		}
		
		public MouseRemoveBlockGoal(FieldMouse mouse, Predicate<BlockState> blockPredicate, int searchDistance, boolean satisfiesHunger) {
			super(blockPredicate, mouse, 1.0, searchDistance, true);
			this.mouse = mouse;
			this.satisfiesHunger = satisfiesHunger;
			
			setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
		}
		
		@Override
		public boolean canUse() {
			if (!mouse.isHungry()) {
				return false;
			}
			
			return super.canUse();
		}
		
		@Override
		public boolean canContinueToUse() {
			return mouse.isHungry() && super.canContinueToUse();
		}
		
		@Override
		protected void destroyBlock(Level level, BlockPos targetBlockPos) {
			super.destroyBlock(level, targetBlockPos);
			
			if (satisfiesHunger) {
				mouse.scheduleNextHunger();
			}
		}
		
		@NotNull
		@Override
		protected BlockPos getMoveToTarget() {
			return blockPos;
		}
		
		@Override
		public double acceptedDistance() {
			return 2f;
		}
	}
	
	private static class MouseCollectSeedsGoal extends Goal {
		
		private final FieldMouse mouse;
		
		private final Predicate<ItemEntity> allowedItemsPredicate;
		
		public MouseCollectSeedsGoal(FieldMouse mouse, Predicate<ItemEntity> allowedItemsPredicate) {
			this.mouse = mouse;
			this.allowedItemsPredicate = allowedItemsPredicate;
			
			this.setFlags(EnumSet.of(Flag.MOVE));
		}
		
		public boolean canUse() {
			if (!mouse.isHungry()) {
				return false;
			}
			
			if (mouse.getTarget() != null || mouse.getLastHurtByMob() != null) {
				return false;
			}
			
			if (mouse.getRandom().nextInt(reducedTickDelay(10)) != 0) {
				return false;
			}
			
			var collectableSeeds = getCollectableSeeds();
			
			return !collectableSeeds.isEmpty();
		}
		
		@Override
		public boolean canContinueToUse() {
			return mouse.isHungry() && super.canContinueToUse();
		}
		
		public void start() {
			var collectableSeeds = getCollectableSeeds();
			
			if (!collectableSeeds.isEmpty()) {
				moveTo(collectableSeeds.get(0));
			}
		}
		
		public void tick() {
			var collectableSeeds = getCollectableSeeds();
			
			if (!collectableSeeds.isEmpty()) {
				moveTo(collectableSeeds.get(0));
			}
		}
		
		private void moveTo(ItemEntity collectableSeed) {
			var navigation = mouse.getNavigation();
			
			navigation.moveTo(collectableSeed, 1.2f);
		}
		
		@NotNull
		private List<ItemEntity> getCollectableSeeds() {
			var searchArea = mouse.getBoundingBox().inflate(8.0D, 8.0D, 8.0D);
			
			return mouse.level().getEntitiesOfClass(ItemEntity.class, searchArea, allowedItemsPredicate);
		}
	}
}
