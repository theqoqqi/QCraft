package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.util.RandomUtils;

public class SummonBoxEntry implements IBoxEntry {
	
	public static final WeightedList<String> GROUND_CREATURES = RandomUtils.createWeightedList(
			new RandomUtils.WeightedEntry<>(10, "minecraft:bat"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:bee"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cat"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:chicken"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cow"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:donkey"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:fox"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:horse"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:llama"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:mooshroom"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:mule"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:ocelot"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:panda"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:parrot"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:pig"),
			new RandomUtils.WeightedEntry<>(1, "minecraft:polar_bear"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:rabbit"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:sheep"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:strider"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:wolf")
	);
	
	public static final WeightedList<String> WATER_CREATURES = RandomUtils.createWeightedList(
			new RandomUtils.WeightedEntry<>(10, "minecraft:cod"),
			new RandomUtils.WeightedEntry<>(2, "minecraft:dolphin"),
			new RandomUtils.WeightedEntry<>(5, "minecraft:pufferfish"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:salmon"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:squid"),
			new RandomUtils.WeightedEntry<>(5, "minecraft:tropical_fish"),
			new RandomUtils.WeightedEntry<>(2, "minecraft:turtle")
	);
	
	public static final WeightedList<String> MONSTERS = RandomUtils.createWeightedList(
			new RandomUtils.WeightedEntry<>(10, "minecraft:blaze"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cave_spider"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:creeper"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:drowned"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:elder_guardian"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:endermite"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:evoker"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:ghast"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:guardian"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:hoglin"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:husk"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:magma_cube"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:phantom"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:piglin_brute"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:pillager"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:ravager"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:shulker"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:silverfish"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:skeleton"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:slime"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:spider"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:stray"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:vex"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:vindicator"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:witch"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:wither_skeleton"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:zoglin"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:zombie"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:zombie_villager")
	);
	
	protected final WeightedList<String> entityNames;
	
	protected final int amount;
	
	private final int spawnRange;
	
	public SummonBoxEntry(String entityName, int spawnRange) {
		this(RandomUtils.createWeightedList(entityName), spawnRange);
	}
	
	public SummonBoxEntry(WeightedList<String> entityNames, int spawnRange) {
		this(entityNames, spawnRange, 1);
	}
	
	public SummonBoxEntry(WeightedList<String> entityNames, int spawnRange, int amount) {
		this.entityNames = entityNames;
		this.spawnRange = spawnRange;
		this.amount = amount;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		String entityName = entityNames.getRandomValue(world.getRandom());
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		
		EntityType.byKey(entityName).ifPresent(entityType -> {
			summonGroup((ServerWorld) world, player, blockPos, lootBox, result, entityType);
		});
		
		return result;
	}
	
	private void summonGroup(ServerWorld world, PlayerEntity player, BlockPos blockPos, ItemStack lootBox, UnpackResult result, EntityType<?> entityType) {
		for (int i = 0; i < amount; i++) {
			BlockPos randomBlockPos = getRandomBlockPos(world, blockPos, entityType);
			Entity entity = summonEntity(world, player, randomBlockPos, lootBox, entityType);
			
			result.addChatMessage(getChatMessage(player, lootBox, entity));
		}
	}
	
	private BlockPos getRandomBlockPos(ServerWorld world, BlockPos blockPos, EntityType<?> entityType) {
		int tries = 20;
		
		do {
			double x = blockPos.getX() + (world.rand.nextDouble() - world.rand.nextDouble()) * spawnRange + 0.5D;
			double y = blockPos.getY() + world.rand.nextInt(3) - 1;
			double z = blockPos.getZ() + (world.rand.nextDouble() - world.rand.nextDouble()) * spawnRange + 0.5D;
			
			BlockPos randomBlockPos = tryGetRandomBlockPos(world, entityType, x, y, z);
			
			if (randomBlockPos != null) {
				return randomBlockPos;
			}
		} while (--tries > 0);
		
		return blockPos;
	}
	
	private BlockPos tryGetRandomBlockPos(ServerWorld world, EntityType<?> entityType, double x, double y, double z) {
		if (!world.hasNoCollisions(entityType.getBoundingBoxWithSizeApplied(x, y, z))) {
			return null;
		}
		
		BlockPos randomBlockPos = new BlockPos(x, y, z);
		
		if (!EntitySpawnPlacementRegistry.canSpawnEntity(entityType, world, SpawnReason.MOB_SUMMONED, randomBlockPos, world.rand)) {
			return null;
		}
		
		return randomBlockPos;
	}
	
	private Entity summonEntity(ServerWorld world, PlayerEntity player, BlockPos blockPos, ItemStack itemStack, EntityType<?> entityType) {
		Entity entity = entityType.spawn(world, itemStack, player, blockPos, SpawnReason.MOB_SUMMONED, true, false);
		
		if (entity instanceof MobEntity) {
			((MobEntity) entity).spawnExplosionParticle();
		}
		
		return entity;
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox, Entity entity) {
		return new TranslationTextComponent(
				"lootBox.entitySummoned",
				player.getDisplayName(),
				lootBox.getTextComponent(),
				entity.getDisplayName()
		);
	}
}
