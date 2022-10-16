package ru.qoqqi.qcraft;

import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.qoqqi.qcraft.items.ModItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemEvents {
	
	@SubscribeEvent
	public static void modelBake(ModelEvent.BakingCompleted event) {
		ModItems.addItemModelProperties();
	}
}
