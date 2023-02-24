package ru.qoqqi.qcraft.biomes.modifiers;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.function.Function;

import ru.qoqqi.qcraft.QCraft;

public class ModBiomeModifierTypes {
	
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS
			= DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, QCraft.MOD_ID);
	
	public static final RegistryObject<Codec<AddSpawnsBiomeModifier>> ADD_SPAWNS_BIOME_MODIFIER_TYPE =
			BIOME_MODIFIER_SERIALIZERS.register(
					"add_spawns",
					() -> RecordCodecBuilder.create(builder -> builder.group(
							MobCategory.CODEC.fieldOf("mob_category").forGetter(AddSpawnsBiomeModifier::mobCategory),
							Biome.LIST_CODEC.fieldOf("biomes").forGetter(AddSpawnsBiomeModifier::biomes),
							new ExtraCodecs.EitherCodec<>(MobSpawnSettings.SpawnerData.CODEC.listOf(), MobSpawnSettings.SpawnerData.CODEC).xmap(
									either -> either.map(Function.identity(), List::of),
									list -> list.size() == 1 ? Either.right(list.get(0)) : Either.left(list)
							).fieldOf("spawners").forGetter(AddSpawnsBiomeModifier::spawners)
					).apply(builder, AddSpawnsBiomeModifier::new))
			);
	
	public static void register(IEventBus eventBus) {
		BIOME_MODIFIER_SERIALIZERS.register(eventBus);
	}
}
