package ru.qoqqi.qcraft.items;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.boxes.LootBoxes;

public class ModItems {
	
	public static final DeferredRegister<Item> ITEMS
			= DeferredRegister.create(ForgeRegistries.ITEMS, QCraft.MOD_ID);
	
	public static final RegistryObject<LootBoxItem> GIFT_BOX_SMALL = registerLootBox("loot_box_small", LootBoxes.LOOT_BOX_SMALL, Rarity.COMMON);
	
	public static final RegistryObject<LootBoxItem> GIFT_BOX_MEDIUM = registerLootBox("loot_box_medium", LootBoxes.LOOT_BOX_MEDIUM, Rarity.UNCOMMON);
	
	public static final RegistryObject<LootBoxItem> GIFT_BOX_LARGE = registerLootBox("loot_box_large", LootBoxes.LOOT_BOX_LARGE, Rarity.RARE);
	
	public static final RegistryObject<LootBoxItem> ATTRIBUTE_BOX = registerLootBox("attribute_box", LootBoxes.ATTRIBUTE_BOX, Rarity.RARE);
	
	public static final RegistryObject<LootBoxItem> NOAHS_BOX = registerLootBox("noahs_box", LootBoxes.NOAHS_BOX, Rarity.RARE);
	
	public static final RegistryObject<LootBoxItem> POSEIDONS_BOX = registerLootBox("poseidons_box", LootBoxes.POSEIDONS_BOX, Rarity.RARE);
	
	public static final RegistryObject<JourneyCompassItem> JOURNEY_COMPASS = registerJourneyCompass("journey_compass");
	
	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
	
	@SuppressWarnings("SameParameterValue")
	private static RegistryObject<JourneyCompassItem> registerJourneyCompass(String name) {
		return ITEMS.register(name, () -> {
			return new JourneyCompassItem(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS).rarity(Rarity.RARE));
		});
	}
	
	private static RegistryObject<Item> register(String name, CreativeModeTab itemGroup) {
		return ITEMS.register(name, () -> new Item(new Item.Properties().tab(itemGroup)));
	}
	
	private static RegistryObject<LootBoxItem> registerLootBox(String name, LootBox lootBox, Rarity rarity) {
		return ITEMS.register(name, () -> new LootBoxItem(
				new Item.Properties().tab(CreativeModeTab.TAB_MISC).rarity(rarity),
				lootBox
		));
	}
	
	private static RegistryObject<Item> register(String name, Block block, CreativeModeTab itemGroup) {
		return ITEMS.register(name, () -> {
			return new BlockItem(block, (new Item.Properties()).tab(itemGroup));
		});
	}
	
	public static void addItemModelProperties() {
		ItemProperties.register(
				JOURNEY_COMPASS.get(),
				new ResourceLocation("angle"),
				new CompassItemPropertyFunction((clientLevel, itemStack, entity) -> {
					if (!(entity instanceof LocalPlayer player)) {
						return null;
					}
					
					return JourneyCompassItem.getNextPlacePosition(player);
				})
		);
	}
}
