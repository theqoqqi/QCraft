package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class StructureBoxEntry implements IBoxEntry {
	
	private final ResourceLocation resourceLocation;
	
	private final String titleTranslationKey;
	
	private final BlockPos offset;
	
	public StructureBoxEntry(ResourceLocation resourceLocation) {
		this(resourceLocation, BlockPos.ZERO);
	}
	
	public StructureBoxEntry(ResourceLocation resourceLocation, BlockPos offset) {
		this.resourceLocation = resourceLocation;
		this.titleTranslationKey = "structure." + resourceLocation.getPath().replace('/', '.');
		this.offset = offset;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		
		ServerLevel serverLevel = (ServerLevel) level;
		StructureTemplateManager templateManager = serverLevel.getStructureManager();
		Optional<StructureTemplate> template = templateManager.get(resourceLocation);
		
		if (template.isEmpty()) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		StructurePlaceSettings placementSettings = new StructurePlaceSettings();
		BlockPos startPos = blockPos.offset(offset);
		RandomSource random = level.getRandom();
		
		template.get().placeInWorld(serverLevel, startPos, startPos, placementSettings, random, 2);
		
		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox));
	}
	
	protected Component getChatMessage(Player player, ItemStack lootBox) {
		return Component.translatable(
				"lootBox.structureSpawn",
				player.getDisplayName(),
				lootBox.getDisplayName(),
				Component.translatable(titleTranslationKey)
		);
	}
}
