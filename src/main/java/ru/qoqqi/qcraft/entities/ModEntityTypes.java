package ru.qoqqi.qcraft.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.items.ModItems;

@Mod.EventBusSubscriber(modid = QCraft.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityTypes {
	
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES
			= DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, QCraft.MOD_ID);
	
	public static final List<RegistryObject<? extends Item>> SPAWN_EGGS = new ArrayList<>();
	
	public static final RegistryObject<EntityType<StoneCrab>> STONE_CRAB = register(
			"stone_crab",
			StoneCrab::new,
			MobCategory.CREATURE,
			0.9f, 0.4f, 10,
			new SpawnEggOptions(0x747474, 0x303b34)
	);
	
	public static final RegistryObject<EntityType<FieldMouse>> FIELD_MOUSE = register(
			"field_mouse",
			FieldMouse::new,
			MobCategory.CREATURE,
			0.5f, 0.4f, 8,
			new SpawnEggOptions(0x797363, 0x4e473b)
	);
	
	public static void register(IEventBus eventBus) {
		ENTITY_TYPES.register(eventBus);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static <T extends Mob> RegistryObject<EntityType<T>> register(
			String name,
			EntityType.EntityFactory<T> factory,
			MobCategory category,
			float width,
			float height,
			int trackingRange,
			SpawnEggOptions spawnEggOptions
	) {
		var entityType = ENTITY_TYPES.register(
				name,
				() -> EntityType.Builder.of(factory, category)
						.sized(width, height)
						.clientTrackingRange(trackingRange)
						.build(name)
		);
		
		if (spawnEggOptions != null) {
			var spawnEgg = ModItems.ITEMS.register(
					name + "_spawn_egg",
					() -> new ForgeSpawnEggItem(
							entityType,
							spawnEggOptions.backgroundColor,
							spawnEggOptions.highlightColor,
							new Item.Properties()
					)
			);
			
			SPAWN_EGGS.add(spawnEgg);
		}
		
		return entityType;
	}
	
	@SubscribeEvent
	public static void addEntityAttributes(EntityAttributeCreationEvent event) {
		event.put(STONE_CRAB.get(), StoneCrab.createAttributes().build());
		event.put(FIELD_MOUSE.get(), FieldMouse.createAttributes().build());
	}
	
	private static class SpawnEggOptions {
		
		int backgroundColor;
		
		int highlightColor;
		
		public SpawnEggOptions(int backgroundColor, int highlightColor) {
			this.backgroundColor = backgroundColor;
			this.highlightColor = highlightColor;
		}
	}
}
