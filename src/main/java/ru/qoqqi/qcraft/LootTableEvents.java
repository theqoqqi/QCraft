package ru.qoqqi.qcraft;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableEvents {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final boolean ENABLE_IN_OTHER_MODS = false;
	
	private static final ResourceLocation injectLootBoxesResourceLocation = new ResourceLocation(QCraft.MOD_ID, "inject/loot_boxes");
	
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
