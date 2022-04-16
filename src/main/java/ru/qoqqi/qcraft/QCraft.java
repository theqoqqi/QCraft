package ru.qoqqi.qcraft;

import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

import ru.qoqqi.qcraft.blocks.ModBlocks;
import ru.qoqqi.qcraft.config.Config;
import ru.qoqqi.qcraft.containers.ModContainers;
import ru.qoqqi.qcraft.items.ModItems;
import ru.qoqqi.qcraft.screens.PuzzleBoxScreen;
import ru.qoqqi.qcraft.tileentities.ModTileEntityTypes;
import ru.qoqqi.qcraft.tileentities.renderers.LootBoxGeneratorTileEntityRenderer;
import ru.qoqqi.qcraft.vanilla.inject.VanillaInjections;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("q_craft")
public class QCraft {
	
	public static final String MOD_ID = "q_craft";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public QCraft() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		eventBus.addListener(this::setup);
		eventBus.addListener(this::enqueueIMC);
		eventBus.addListener(this::processIMC);
		eventBus.addListener(this::doClientStuff);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		
		MinecraftForge.EVENT_BUS.register(this);
		
		ModBlocks.register(eventBus);
		ModItems.register(eventBus);
		ModContainers.register(eventBus);
		ModTileEntityTypes.register(eventBus);
	}
	
	static {
		VanillaInjections.setLoadScreenColor(31, 31, 31);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("HELLO FROM PREINIT");
	}
	
	private void doClientStuff(final FMLClientSetupEvent event) {
		ScreenManager.registerFactory(ModContainers.PUZZLE_BOX_CONTAINER.get(), PuzzleBoxScreen::new);
		ClientRegistry.bindTileEntityRenderer(ModTileEntityTypes.LOOT_BOX_GENERATOR.get(), LootBoxGeneratorTileEntityRenderer::new);
		
		LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
	}
	
	private void enqueueIMC(final InterModEnqueueEvent event) {
		
		InterModComms.sendTo("q_craft", "helloworld", () -> {
			LOGGER.info("Hello world from the MDK");
			return "Hello world";
		});
	}
	
	private void processIMC(final InterModProcessEvent event) {
		
		LOGGER.info("Got IMC {}", event.getIMCStream().
				map(m -> m.getMessageSupplier().get()).
				collect(Collectors.toList()));
	}
	
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event) {
		
		LOGGER.info("HELLO from server starting");
	}
}
