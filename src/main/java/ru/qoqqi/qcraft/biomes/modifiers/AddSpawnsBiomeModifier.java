package ru.qoqqi.qcraft.biomes.modifiers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;
import java.util.function.Function;

public record AddSpawnsBiomeModifier(
		MobCategory mobCategory,
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

	public static final Codec<AddSpawnsBiomeModifier> CODEC = RecordCodecBuilder.create(builder -> builder.group(
			MobCategory.CODEC.fieldOf("mob_category")
					.forGetter(AddSpawnsBiomeModifier::mobCategory),
			Biome.LIST_CODEC.fieldOf("biomes")
					.forGetter(AddSpawnsBiomeModifier::biomes),
			SPAWNER_CODEC.fieldOf("spawners")
					.forGetter(AddSpawnsBiomeModifier::spawners)
	).apply(builder, AddSpawnsBiomeModifier::new));

	@Override
	public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && this.biomes.contains(biome)) {
			MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
			for (MobSpawnSettings.SpawnerData spawner : this.spawners) {
				spawns.addSpawn(mobCategory, spawner);
			}
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return CODEC;
	}
}
