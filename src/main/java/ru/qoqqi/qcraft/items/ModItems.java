package ru.qoqqi.qcraft.items;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

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
	
	public static void register(IEventBus eventBus) {
		ITEMS.register(eventBus);
	}
	
	static {
		System.out.println("ModItems static {}");
	}
	
	private static RegistryObject<Item> register(String name, ItemGroup itemGroup) {
		return ITEMS.register(name, () -> new Item(new Item.Properties().group(itemGroup)));
	}
	
	private static RegistryObject<LootBoxItem> registerLootBox(String name, LootBox lootBox, Rarity rarity) {
		return ITEMS.register(name, () -> new LootBoxItem(
				new Item.Properties().group(ItemGroup.MISC).rarity(rarity),
				lootBox
		));
	}
	
	private static RegistryObject<Item> register(String name, Block block, ItemGroup itemGroup) {
		return ITEMS.register(name, () -> {
			return new BlockItem(block, (new Item.Properties()).group(itemGroup));
		});
	}
}
