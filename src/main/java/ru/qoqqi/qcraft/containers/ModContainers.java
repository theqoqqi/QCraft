package ru.qoqqi.qcraft.containers;

import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import ru.qoqqi.qcraft.QCraft;

public class ModContainers {
	
	public static final DeferredRegister<ContainerType<?>> CONTAINERS
			= DeferredRegister.create(ForgeRegistries.CONTAINERS, QCraft.MOD_ID);
	
	public static final RegistryObject<ContainerType<PuzzleBoxContainer>> PUZZLE_BOX_CONTAINER = register("puzzle_box", PuzzleBoxContainer::new);
	
	public static void register(IEventBus eventBus) {
		CONTAINERS.register(eventBus);
	}
	
	private static <T extends Container> RegistryObject<ContainerType<T>> register(String name, ContainerType.IFactory<T> factory) {
		return CONTAINERS.register(name, () -> new ContainerType<>(factory));
	}
}
