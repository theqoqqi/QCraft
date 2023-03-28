package ru.qoqqi.qcraft;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LootTableEvents {
	
	private static final boolean ENABLE_IN_OTHER_MODS = false;
	
	private static final ResourceLocation injectLootBoxesResourceLocation = new ResourceLocation(QCraft.MOD_ID, "inject/loot_boxes");
	
	@SubscribeEvent
	public static void modifyLootTables(@Nonnull final LootTableLoadEvent event) {
		String namespace = event.getName().getNamespace();
		
		if (ENABLE_IN_OTHER_MODS && !"minecraft".equals(namespace)) {
			LootTables lootTableManager = event.getLootTableManager();
			LootTable injectLootTable = lootTableManager.get(injectLootBoxesResourceLocation);
			LootPool injectPool = injectLootTable.getPool("loot_boxes");
			
			event.getTable().addPool(injectPool);
		}
	}
}
