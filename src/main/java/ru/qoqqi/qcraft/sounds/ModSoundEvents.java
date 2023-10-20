package ru.qoqqi.qcraft.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;

public class ModSoundEvents {

	public static final DeferredRegister<SoundEvent> SOUND_EVENTS
			= DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, QCraft.MOD_ID);

	public static final RegistryObject<SoundEvent> JELLY_BLOB_INFLATE = register("entities.jelly_blob.inflate");

	public static final RegistryObject<SoundEvent> JELLY_BLOB_BLOW_UP = register("entities.jelly_blob.blow_up");

	private static RegistryObject<SoundEvent> register(String name) {
		var resourceLocation = new ResourceLocation(QCraft.MOD_ID, name);

		return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(resourceLocation));
	}

	public static void register(IEventBus eventBus) {
		SOUND_EVENTS.register(eventBus);
	}
}
