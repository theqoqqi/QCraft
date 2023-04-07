package ru.qoqqi.qcraft.biomes.modifiers;

import com.mojang.serialization.Codec;

import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import ru.qoqqi.qcraft.QCraft;

public class ModBiomeModifierTypes {
	
	public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS
			= DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, QCraft.MOD_ID);
	
	static {
		BIOME_MODIFIER_SERIALIZERS.register("add_spawns", () -> AddSpawnsBiomeModifier.CODEC);
		BIOME_MODIFIER_SERIALIZERS.register("add_custom_spawns", () -> AddCustomSpawnsBiomeModifier.CODEC);
	}
	
	public static void register(IEventBus eventBus) {
		BIOME_MODIFIER_SERIALIZERS.register(eventBus);
	}
}
