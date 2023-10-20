package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.util.IntRange;
import ru.qoqqi.qcraft.util.WeightedList;

public class SummonBoxEntry implements IBoxEntry {

	public static final WeightedList<String> GROUND_CREATURES = WeightedList.create(
			new WeightedList.WeightedEntry<>(10, "minecraft:allay"),
			new WeightedList.WeightedEntry<>(10, "minecraft:axolotl"),
			new WeightedList.WeightedEntry<>(10, "minecraft:bat"),
			new WeightedList.WeightedEntry<>(10, "minecraft:bee"),
			new WeightedList.WeightedEntry<>(10, "minecraft:cat"),
			new WeightedList.WeightedEntry<>(10, "minecraft:chicken"),
			new WeightedList.WeightedEntry<>(10, "minecraft:cow"),
			new WeightedList.WeightedEntry<>(10, "minecraft:donkey"),
			new WeightedList.WeightedEntry<>(10, "minecraft:fox"),
			new WeightedList.WeightedEntry<>(10, "minecraft:frog"),
			new WeightedList.WeightedEntry<>(10, "minecraft:goat"),
			new WeightedList.WeightedEntry<>(10, "minecraft:horse"),
			new WeightedList.WeightedEntry<>(10, "minecraft:llama"),
			new WeightedList.WeightedEntry<>(10, "minecraft:mooshroom"),
			new WeightedList.WeightedEntry<>(10, "minecraft:mule"),
			new WeightedList.WeightedEntry<>(10, "minecraft:ocelot"),
			new WeightedList.WeightedEntry<>(10, "minecraft:panda"),
			new WeightedList.WeightedEntry<>(10, "minecraft:parrot"),
			new WeightedList.WeightedEntry<>(10, "minecraft:pig"),
			new WeightedList.WeightedEntry<>(1, "minecraft:polar_bear"),
			new WeightedList.WeightedEntry<>(10, "minecraft:rabbit"),
			new WeightedList.WeightedEntry<>(10, "minecraft:sheep"),
			new WeightedList.WeightedEntry<>(10, "minecraft:strider"),
			new WeightedList.WeightedEntry<>(10, "minecraft:tadpole"),
			new WeightedList.WeightedEntry<>(10, "minecraft:wolf")
	);

	public static final WeightedList<String> WATER_CREATURES = WeightedList.create(
			new WeightedList.WeightedEntry<>(10, "minecraft:cod"),
			new WeightedList.WeightedEntry<>(2, "minecraft:dolphin"),
			new WeightedList.WeightedEntry<>(5, "minecraft:glow_squid"),
			new WeightedList.WeightedEntry<>(5, "minecraft:pufferfish"),
			new WeightedList.WeightedEntry<>(10, "minecraft:salmon"),
			new WeightedList.WeightedEntry<>(10, "minecraft:squid"),
			new WeightedList.WeightedEntry<>(5, "minecraft:tropical_fish"),
			new WeightedList.WeightedEntry<>(2, "minecraft:turtle")
	);

	public static final WeightedList<String> MONSTERS = WeightedList.create(
			new WeightedList.WeightedEntry<>(10, "minecraft:blaze"),
			new WeightedList.WeightedEntry<>(10, "minecraft:cave_spider"),
			new WeightedList.WeightedEntry<>(10, "minecraft:creeper"),
			new WeightedList.WeightedEntry<>(10, "minecraft:drowned"),
			new WeightedList.WeightedEntry<>(10, "minecraft:elder_guardian"),
			new WeightedList.WeightedEntry<>(10, "minecraft:endermite"),
			new WeightedList.WeightedEntry<>(10, "minecraft:evoker"),
			new WeightedList.WeightedEntry<>(10, "minecraft:ghast"),
			new WeightedList.WeightedEntry<>(10, "minecraft:guardian"),
			new WeightedList.WeightedEntry<>(10, "minecraft:hoglin"),
			new WeightedList.WeightedEntry<>(10, "minecraft:husk"),
			new WeightedList.WeightedEntry<>(10, "minecraft:magma_cube"),
			new WeightedList.WeightedEntry<>(10, "minecraft:phantom"),
			new WeightedList.WeightedEntry<>(10, "minecraft:piglin_brute"),
			new WeightedList.WeightedEntry<>(10, "minecraft:pillager"),
			new WeightedList.WeightedEntry<>(10, "minecraft:ravager"),
			new WeightedList.WeightedEntry<>(10, "minecraft:shulker"),
			new WeightedList.WeightedEntry<>(10, "minecraft:silverfish"),
			new WeightedList.WeightedEntry<>(10, "minecraft:skeleton"),
			new WeightedList.WeightedEntry<>(10, "minecraft:slime"),
			new WeightedList.WeightedEntry<>(10, "minecraft:spider"),
			new WeightedList.WeightedEntry<>(10, "minecraft:stray"),
			new WeightedList.WeightedEntry<>(10, "minecraft:vex"),
			new WeightedList.WeightedEntry<>(10, "minecraft:vindicator"),
			new WeightedList.WeightedEntry<>(10, "minecraft:warden"),
			new WeightedList.WeightedEntry<>(10, "minecraft:witch"),
			new WeightedList.WeightedEntry<>(10, "minecraft:wither_skeleton"),
			new WeightedList.WeightedEntry<>(10, "minecraft:zoglin"),
			new WeightedList.WeightedEntry<>(10, "minecraft:zombie"),
			new WeightedList.WeightedEntry<>(10, "minecraft:zombie_villager")
	);

	protected final WeightedList<String> entityNames;

	protected final IntRange amountRange;

	private final int spawnRange;

	public SummonBoxEntry(String entityName, int spawnRange) {
		this(WeightedList.create(entityName), spawnRange);
	}

	public SummonBoxEntry(WeightedList<String> entityNames, int spawnRange) {
		this(entityNames, spawnRange, 1);
	}

	public SummonBoxEntry(WeightedList<String> entityNames, int spawnRange, int amount) {
		this(entityNames, spawnRange, IntRange.of(amount, amount));
	}

	public SummonBoxEntry(WeightedList<String> entityNames, int spawnRange, IntRange amountRange) {
		this.entityNames = entityNames;
		this.spawnRange = spawnRange;
		this.amountRange = amountRange;
	}

	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		String entityName = entityNames.getRandomValue(level.getRandom());

		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);

		EntityType.byString(entityName).ifPresent(entityType -> {
			summonGroup((ServerLevel) level, player, blockPos, lootBox, result, entityType);
		});

		return result;
	}

	private void summonGroup(ServerLevel level, Player player, BlockPos blockPos, ItemStack lootBox, UnpackResult result, EntityType<?> entityType) {
		var amount = amountRange.getRandomValue(level.random);

		for (int i = 0; i < amount; i++) {
			BlockPos randomBlockPos = getRandomBlockPos(level, blockPos, entityType);
			Entity entity = summonEntity(level, player, randomBlockPos, lootBox, entityType);

			result.addChatMessage(getChatMessage(player, lootBox, entity));
		}
	}

	private BlockPos getRandomBlockPos(ServerLevel level, BlockPos blockPos, EntityType<?> entityType) {
		int tries = 20;

		do {
			double x = blockPos.getX() + (level.random.nextDouble() - level.random.nextDouble()) * spawnRange + 0.5D;
			double y = blockPos.getY() + level.random.nextInt(3) - 1;
			double z = blockPos.getZ() + (level.random.nextDouble() - level.random.nextDouble()) * spawnRange + 0.5D;

			BlockPos randomBlockPos = tryGetRandomBlockPos(level, entityType, x, y, z);

			if (randomBlockPos != null) {
				return randomBlockPos;
			}
		} while (--tries > 0);

		return blockPos;
	}

	private BlockPos tryGetRandomBlockPos(ServerLevel level, EntityType<?> entityType, double x, double y, double z) {
		if (!level.noCollision(entityType.getAABB(x, y, z))) {
			return null;
		}

		BlockPos randomBlockPos = BlockPos.containing(x, y, z);

		if (!SpawnPlacements.checkSpawnRules(entityType, level, MobSpawnType.MOB_SUMMONED, randomBlockPos, level.random)) {
			return null;
		}

		return randomBlockPos;
	}

	private Entity summonEntity(ServerLevel level, Player player, BlockPos blockPos, ItemStack itemStack, EntityType<?> entityType) {
		Entity entity = entityType.spawn(level, itemStack, player, blockPos, MobSpawnType.MOB_SUMMONED, true, false);

		if (entity instanceof Mob) {
			((Mob) entity).spawnAnim();
		}

		return entity;
	}

	protected Component getChatMessage(Player player, ItemStack lootBox, Entity entity) {
		return Component.translatable(
				"lootBox.entitySummoned",
				player.getDisplayName(),
				lootBox.getDisplayName(),
				entity.getDisplayName()
		);
	}
}
