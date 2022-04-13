package ru.qoqqi.qcraft.items;

import net.minecraft.item.BlockItem;

import ru.qoqqi.qcraft.blocks.LootBoxBlock;
import ru.qoqqi.qcraft.boxes.LootBox;

public class LootBoxBlockItem extends BlockItem {
	
	private final LootBox lootBox;
	
	public LootBoxBlockItem(LootBoxBlock block, Properties properties, LootBox lootBox) {
		super(block, properties);
		this.lootBox = lootBox;
	}
}
