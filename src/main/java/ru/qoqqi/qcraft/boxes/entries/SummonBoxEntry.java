package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
	
	public static final WeightedList<String> CREATURES = RandomUtils.createWeightedList(
			new RandomUtils.WeightedEntry<>(10, "minecraft:bat"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:bee"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cat"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:chicken"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cod"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:cow"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:dolphin"),
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
			new RandomUtils.WeightedEntry<>(1, "minecraft:pufferfish"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:rabbit"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:salmon"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:sheep"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:squid"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:strider"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:tropical_fish"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:turtle"),
			new RandomUtils.WeightedEntry<>(10, "minecraft:wolf")
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
	
	public SummonBoxEntry(String entityName) {
		this(RandomUtils.createWeightedList(entityName));
	}
	
	public SummonBoxEntry(WeightedList<String> entityNames) {
		this(entityNames, 1);
	}
	
	public SummonBoxEntry(WeightedList<String> entityNames, int amount) {
		this.entityNames = entityNames;
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
			Entity entity = summonEntity(world, player, blockPos, lootBox, entityType);
			
			result.addChatMessage(getChatMessage(player, lootBox, entity));
		}
	}
	
	private Entity summonEntity(ServerWorld world, PlayerEntity player, BlockPos blockPos, ItemStack itemStack, EntityType<?> entityType) {
		return entityType.spawn(world, itemStack, player, blockPos, SpawnReason.MOB_SUMMONED, true, false);
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
