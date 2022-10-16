package ru.qoqqi.qcraft;

import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.qoqqi.qcraft.particles.ModParticleTypes;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleEvents {
	
	@SubscribeEvent
	public static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
		ModParticleTypes.registerProviders(event);
	}
}
