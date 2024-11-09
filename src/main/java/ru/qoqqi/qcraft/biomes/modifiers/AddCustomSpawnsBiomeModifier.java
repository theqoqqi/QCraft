package ru.qoqqi.qcraft.biomes.modifiers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.function.Function;

import ru.qoqqi.qcraft.spawners.CustomNaturalSpawner;
import ru.qoqqi.qcraft.spawners.SpawnerType;

public record AddCustomSpawnsBiomeModifier(
		SpawnerType spawnerType,
		HolderSet<Biome> biomes,
		List<MobSpawnSettings.SpawnerData> spawners
) implements BiomeModifier {

	private static final Codec<List<MobSpawnSettings.SpawnerData>> SPAWNER_CODEC =
			new ExtraCodecs.EitherCodec<>(
					MobSpawnSettings.SpawnerData.CODEC.listOf(),
					MobSpawnSettings.SpawnerData.CODEC
			).xmap(
					either -> either.map(Function.identity(), List::of),
					list -> list.size() == 1
							? Either.right(list.get(0))
							: Either.left(list)
			);

	public static final Codec<AddCustomSpawnsBiomeModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			SpawnerType.CODEC.fieldOf("spawner_type")
					.forGetter(AddCustomSpawnsBiomeModifier::spawnerType),
			Biome.LIST_CODEC.fieldOf("biomes")
					.forGetter(AddCustomSpawnsBiomeModifier::biomes),
			SPAWNER_CODEC.fieldOf("spawners")
					.forGetter(AddCustomSpawnsBiomeModifier::spawners)
	).apply(builder, AddCustomSpawnsBiomeModifier::new));

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && this.biomes.contains(biome)) {
			CustomNaturalSpawner.addSpawns(spawnerType, biome, spawners);
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
