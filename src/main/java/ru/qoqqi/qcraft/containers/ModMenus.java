package ru.qoqqi.qcraft.containers;

import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;

public class ModMenus {

	public static final DeferredRegister<MenuType<?>> MENU_TYPES
			= DeferredRegister.create(ForgeRegistries.MENU_TYPES, QCraft.MOD_ID);

	public static final RegistryObject<MenuType<PuzzleBoxMenu>> PUZZLE_BOX_MENU = register("puzzle_box", PuzzleBoxMenu::new);

	public static void register(IEventBus eventBus) {
		MENU_TYPES.register(eventBus);
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> register(String name, MenuType.MenuSupplier<T> factory) {
		return MENU_TYPES.register(name, () -> new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS));
	}
}
