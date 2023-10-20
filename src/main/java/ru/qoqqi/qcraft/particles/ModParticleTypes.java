package ru.qoqqi.qcraft.particles;

import com.mojang.serialization.Codec;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import ru.qoqqi.qcraft.QCraft;

public class ModParticleTypes {
	
	public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES
			= DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, QCraft.MOD_ID);
	
	public static final RegistryObject<SimpleParticleType> LOOT_BOX_GENERATOR = register("loot_box_generator");
	
	public static final RegistryObject<ParticleType<JellyBlobPieceParticleOption>> JELLY_BLOB =
			register("jelly_blob_piece", JellyBlobPieceParticleOption.DESERIALIZER, JellyBlobPieceParticle::codec);
	
	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<SimpleParticleType> register(String name) {
		return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
	}
	
	@SuppressWarnings("SameParameterValue")
	private static <T extends ParticleOptions> RegistryObject<ParticleType<T>> register(
			String name,
			ParticleOptions.Deserializer<T> deserializer,
			final Function<ParticleType<T>, Codec<T>> codec
	) {
		return PARTICLE_TYPES.register(name, () -> new ParticleType<>(false, deserializer) {
			@NotNull
			public Codec<T> codec() {
				return codec.apply(this);
			}
		});
	}
	
	public static void register(IEventBus eventBus) {
		PARTICLE_TYPES.register(eventBus);
	}
	
	public static void registerProviders(RegisterParticleProvidersEvent event) {
		event.register(ModParticleTypes.LOOT_BOX_GENERATOR.get(), LootBoxGeneratorParticle.Provider::new);
		event.register(ModParticleTypes.JELLY_BLOB.get(), JellyBlobPieceParticle.Provider::new);
	}
}
