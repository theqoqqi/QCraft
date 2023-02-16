package ru.qoqqi.qcraft.structures;

import com.mojang.serialization.Codec;

import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.config.Config;

public class ModStructureTypes {
	public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
			DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, QCraft.MOD_ID);
	
	public static final RegistryObject<StructureType<CustomJigsawStructure>>
			CUSTOM_JIGSAW = register("custom_jigsaw", CustomJigsawStructure.CODEC);
	
	public static final RegistryObject<StructureType<JourneyStructure>>
			JOURNEY_PLACE = register("journey_place", JourneyStructure.CODEC);
	
	private static <T extends Structure> RegistryObject<StructureType<T>> register(String name, Codec<T> codec) {
		StructureType<T> structureType = () -> codec;
		return STRUCTURE_TYPES.register(name, () -> structureType);
	}
	
	public static void register(IEventBus eventBus) {
		STRUCTURE_TYPES.register(eventBus);
	}
}
