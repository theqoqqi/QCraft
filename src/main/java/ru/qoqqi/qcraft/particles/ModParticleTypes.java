package ru.qoqqi.qcraft.particles;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;

public class ModParticleTypes {
	
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES
			= DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, QCraft.MOD_ID);

	public static final RegistryObject<SimpleParticleType> LOOT_BOX_GENERATOR = register("loot_box_generator");
	
	private static RegistryObject<SimpleParticleType> register(String name) {
		return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
	}
	
	public static void register(IEventBus eventBus) {
		PARTICLE_TYPES.register(eventBus);
	}
	
	public static void registerProviders(RegisterParticleProvidersEvent event) {
		event.register(ModParticleTypes.LOOT_BOX_GENERATOR.get(), LootBoxGeneratorParticle.Provider::new);
	}
}
