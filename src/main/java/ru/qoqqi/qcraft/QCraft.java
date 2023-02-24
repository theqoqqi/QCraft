package ru.qoqqi.qcraft;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.biomes.modifiers.ModBiomeModifierTypes;
import ru.qoqqi.qcraft.blockentities.ModBlockEntityTypes;
import ru.qoqqi.qcraft.blockentities.renderers.ItemPedestalBlockEntityRenderer;
import ru.qoqqi.qcraft.blocks.ModBlocks;
import ru.qoqqi.qcraft.config.Config;
import ru.qoqqi.qcraft.containers.ModMenus;
import ru.qoqqi.qcraft.entities.ModEntityTypes;
import ru.qoqqi.qcraft.entities.ModSpawnPlacements;
import ru.qoqqi.qcraft.entities.renderers.FieldMouseRenderer;
import ru.qoqqi.qcraft.entities.renderers.StoneCrabRenderer;
import ru.qoqqi.qcraft.items.ModItems;
import ru.qoqqi.qcraft.loot.GlobalLootModifiers;
import ru.qoqqi.qcraft.network.ModPacketHandler;
import ru.qoqqi.qcraft.particles.ModParticleTypes;
import ru.qoqqi.qcraft.screens.PuzzleBoxScreen;
import ru.qoqqi.qcraft.structures.ModStructureTypes;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("q_craft")
public class QCraft {
	
	public static final String MOD_ID = "q_craft";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static MinecraftServer lastStartedServer;
	
	public QCraft() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		eventBus.addListener(this::setup);
		eventBus.addListener(this::loadComplete);
		eventBus.addListener(this::doClientStuff);
		eventBus.addListener(this::initCreativeTabs);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		
		MinecraftForge.EVENT_BUS.register(this);
		
		ModBlocks.register(eventBus);
		ModBlockEntityTypes.register(eventBus);
		ModItems.register(eventBus);
		ModEntityTypes.register(eventBus);
		ModParticleTypes.register(eventBus);
		ModStructureTypes.register(eventBus);
		ModBiomeModifierTypes.register(eventBus);
		ModMenus.register(eventBus);
		ModCriteriaTriggers.register();
		
		GlobalLootModifiers.register(eventBus);
	}
	
	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("QCraft - setup");
		ModPacketHandler.init();
		
		event.enqueueWork(ModSpawnPlacements::register);
	}
	
	private void doClientStuff(final FMLClientSetupEvent event) {
		LOGGER.info("QCraft - doClientStuff");
		MenuScreens.register(ModMenus.PUZZLE_BOX_MENU.get(), PuzzleBoxScreen::new);
		BlockEntityRenderers.register(ModBlockEntityTypes.LOOT_BOX_GENERATOR.get(), ItemPedestalBlockEntityRenderer::new);
		BlockEntityRenderers.register(ModBlockEntityTypes.JOURNEY_REWARD.get(), ItemPedestalBlockEntityRenderer::new);
		EntityRenderers.register(ModEntityTypes.STONE_CRAB.get(), StoneCrabRenderer::new);
		EntityRenderers.register(ModEntityTypes.FIELD_MOUSE.get(), FieldMouseRenderer::new);
	}
	
	private void loadComplete(final FMLLoadCompleteEvent event) {
		LOGGER.info("QCraft - loadComplete");
	
	}
	
	private void initCreativeTabs(final CreativeModeTabEvent.BuildContents event) {
		LOGGER.info("QCraft - initCreativeTabs");
		
		if (event.getTab() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
			event.accept(ModBlocks.PUZZLE_BOX_EASY);
			event.accept(ModBlocks.PUZZLE_BOX_NORMAL);
			event.accept(ModBlocks.PUZZLE_BOX_HARD);
			event.accept(ModBlocks.PANDORAS_BOX);
			event.accept(ModBlocks.LOOT_BOX_GENERATOR_BLOCK);
			event.accept(ModBlocks.TRAVELERS_HOME_JOURNEY_REWARD_BLOCK);
			event.accept(ModBlocks.FORTUNE_ISLAND_JOURNEY_REWARD_BLOCK);
			event.accept(ModBlocks.JUNGLE_TEMPLE_JOURNEY_REWARD_BLOCK);
			event.accept(ModBlocks.MANGROVE_TEMPLE_JOURNEY_REWARD_BLOCK);
			event.accept(ModBlocks.PANDORAS_TEMPLE_JOURNEY_REWARD_BLOCK);
		}
		
		if (event.getTab() == CreativeModeTabs.BUILDING_BLOCKS) {
			event.accept(ModBlocks.OAK_PLATE);
			event.accept(ModBlocks.BIRCH_PLATE);
			event.accept(ModBlocks.ACACIA_PLATE);
			event.accept(ModBlocks.JUNGLE_PLATE);
			event.accept(ModBlocks.SPRUCE_PLATE);
			event.accept(ModBlocks.DARK_OAK_PLATE);
			event.accept(ModBlocks.MANGROVE_PLATE);
		}
		
		if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
			event.accept(ModItems.GIFT_BOX_SMALL);
			event.accept(ModItems.GIFT_BOX_MEDIUM);
			event.accept(ModItems.GIFT_BOX_LARGE);
			event.accept(ModItems.ATTRIBUTE_BOX);
			event.accept(ModItems.NOAHS_BOX);
			event.accept(ModItems.POSEIDONS_BOX);
			event.accept(ModItems.JOURNEY_COMPASS);
		}
		
		if (event.getTab() == CreativeModeTabs.SPAWN_EGGS) {
			ModEntityTypes.SPAWN_EGGS.forEach(event::accept);
		}
	}
	
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("QCraft - onServerStarting");
		lastStartedServer = event.getServer();
	}
	
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public static MinecraftServer getLastStartedServer() {
		return lastStartedServer;
	}
}
