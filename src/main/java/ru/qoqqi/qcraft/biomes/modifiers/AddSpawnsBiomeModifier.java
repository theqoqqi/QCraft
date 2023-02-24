package ru.qoqqi.qcraft.biomes.modifiers;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;

public record AddSpawnsBiomeModifier(
		MobCategory mobCategory,
		HolderSet<Biome> biomes,
		List<MobSpawnSettings.SpawnerData> spawners
) implements BiomeModifier {
	
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
		return ModBiomeModifierTypes.ADD_SPAWNS_BIOME_MODIFIER_TYPE.get();
	}
}
