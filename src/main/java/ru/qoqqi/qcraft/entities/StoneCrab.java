package ru.qoqqi.qcraft.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import org.jetbrains.annotations.NotNull;

public class StoneCrab extends Animal {
	
	private static final int[] animateJawsPossibleRepeatTimes = {1, 1, 1, 1, 1, 1, 2, 2, 2, 3};
	
	private float animateJawsAt;
	
	private int animateJawsTimes;
	
	private float jawsAnimationTicks;
	
	public StoneCrab(EntityType<? extends StoneCrab> entityType, Level level) {
		super(entityType, level);
		
		scheduleJawsAnimation(0f);
	}
	
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.IRON_NUGGET), false));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}
	
	public static <T extends Animal> boolean checkStoneCrabSpawnRules(
			EntityType<T> entityType,
			ServerLevelAccessor levelAccessor,
			MobSpawnType spawnType,
			BlockPos blockPos,
			RandomSource random
	) {
		var spawnY = blockPos.getY();
		
		if (spawnY >= levelAccessor.getLevel().getSeaLevel()) {
			return checkOvergroundSpawnRules(entityType, levelAccessor, spawnType, blockPos, random);
		}
		
		return true;
	}
	
	private static <T extends Animal> boolean checkOvergroundSpawnRules(
			@SuppressWarnings("unused") EntityType<T> entityType,
			ServerLevelAccessor levelAccessor,
			@SuppressWarnings("unused") MobSpawnType spawnType,
			BlockPos blockPos,
			RandomSource random
	) {
		var blockStateBelow = levelAccessor.getBlockState(blockPos.below());
		var biome = levelAccessor.getBiome(blockPos);
		
		if (blockStateBelow.is(Tags.Blocks.STONE)) {
			return true;
		}
		
		if (biome.is(BiomeTags.IS_BEACH)) {
			return random.nextFloat() < 0.5f;
		}
		
		if (blockStateBelow.is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
			return random.nextFloat() < 0.1f;
		}
		
		return false;
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.08)
				.add(Attributes.ARMOR, 20.0)
				.add(Attributes.ARMOR_TOUGHNESS, 4.0);
	}
	
	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		if (!source.isMagic() && !source.isExplosion()) {
			if (source.getDirectEntity() instanceof LivingEntity attacker) {
				if (random.nextFloat() < 0.2f) {
					attacker.hurt(DamageSource.thorns(this), 1f);
				}
			}
		}
		
		return super.hurt(source, amount);
	}
	
	public float getJawsRotationProgress(float ageInTicks) {
		var currentDuration = ageInTicks - animateJawsAt;
		var progress = currentDuration / jawsAnimationTicks;
		var inProgress = Math.min(1, progress * 2);
		var outProgress = Math.min(1, (1 - progress) * 2);
		
		return progress < 0.5f ? inProgress : outProgress;
	}
	
	public boolean updateJawsAnimationState(float ageInTicks) {
		var animateJawsStarted = animateJawsAt <= ageInTicks;
		var animateJawsFinished = animateJawsAt + jawsAnimationTicks <= ageInTicks;
		var isAnimatingJaws = animateJawsStarted && !animateJawsFinished;
		
		if (animateJawsFinished) {
			scheduleJawsAnimation(ageInTicks);
		}
		
		return isAnimatingJaws;
	}
	
	private void scheduleJawsAnimation(float ageInTicks) {
		if (--animateJawsTimes > 0) {
			animateJawsAt = ageInTicks;
			return;
		}
		
		animateJawsTimes = getRandomValue(animateJawsPossibleRepeatTimes);
		animateJawsAt = ageInTicks + getRandomValue(50f, 200f);
		jawsAnimationTicks = getRandomValue(8f, 16f);
	}
	
	private float getRandomValue(float min, float max) {
		return (float) Math.random() * (max - min) + min;
	}
	
	@SuppressWarnings("SameParameterValue")
	private int getRandomValue(int[] values) {
		return values[random.nextInt(values.length)];
	}
	
	@SuppressWarnings("deprecation")
	public boolean canBreatheUnderwater() {
		return true;
	}
	
	protected SoundEvent getAmbientSound() {
		return SoundEvents.DRIPSTONE_BLOCK_FALL;
	}
	
	protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
		return SoundEvents.DRIPSTONE_BLOCK_HIT;
	}
	
	protected SoundEvent getDeathSound() {
		return SoundEvents.DRIPSTONE_BLOCK_BREAK;
	}
	
	protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState block) {
		this.playSound(SoundEvents.DRIPSTONE_BLOCK_STEP, 0.15F, 1.0F);
	}
	
	protected float getSoundVolume() {
		return 0.5F;
	}
	
	public StoneCrab getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
		return ModEntityTypes.STONE_CRAB.get().create(level);
	}
	
	protected float getStandingEyeHeight(@NotNull Pose pose, @NotNull EntityDimensions size) {
		return size.height * 0.8f;
	}
}
