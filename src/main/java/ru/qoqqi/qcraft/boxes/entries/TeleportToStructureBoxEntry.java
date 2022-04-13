package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.registries.ForgeRegistries;

public class TeleportToStructureBoxEntry extends TeleportBoxEntry {
	
	public TeleportToStructureBoxEntry(String structureName) {
		super((playerEntity, world) -> {
			Structure<?> structure = getStructure(structureName);
			
			if (structure == null) {
				return null;
			}
			
			return world.getStructureLocation(structure, playerEntity.getPosition(), 200, false);
		}, () -> {
			Structure<?> structure = getStructure(structureName);
			
			if (structure == null) {
				return null;
			}
			
			return new StringTextComponent(structure.getStructureName());
		});
	}
	
	private static Structure<?> getStructure(String structureName) {
		ResourceLocation resourceLocation = new ResourceLocation(structureName);
		return ForgeRegistries.STRUCTURE_FEATURES.getValue(resourceLocation);
	}
}
