package ru.qoqqi.qcraft;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableEvents {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final boolean ENABLE_IN_OTHER_MODS = false;
	
	private static final ResourceLocation injectLootBoxesResourceLocation = new ResourceLocation("q_craft", "inject/loot_boxes");
	
	@SubscribeEvent
	public static void modifyLootTables(@Nonnull final LootTableLoadEvent event) {
		String namespace = event.getName().getNamespace();
		
		if (ENABLE_IN_OTHER_MODS && !"minecraft".equals(namespace)) {
			LootTableManager lootTableManager = event.getLootTableManager();
			LootTable injectLootTable = lootTableManager.getLootTableFromLocation(injectLootBoxesResourceLocation);
			LootPool injectPool = injectLootTable.getPool("loot_boxes");
			
			event.getTable().addPool(injectPool);
		}
	}
}
