package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

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
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		
		ServerWorld serverWorld = (ServerWorld) world;
		TemplateManager templatemanager = serverWorld.getStructureTemplateManager();
		Template template = templatemanager.getTemplate(resourceLocation);
		
		if (template == null) {
			return UnpackResult.resultFail(lootBox, player);
		}
		
		PlacementSettings placementSettings = new PlacementSettings();
		BlockPos startPos = blockPos.add(offset);
		Random random = world.getRandom();
		
		template.func_237152_b_(serverWorld, startPos, placementSettings, random);
		
		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox));
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox) {
		return new TranslationTextComponent(
				"lootBox.structureSpawn",
				player.getDisplayName(),
				lootBox.getTextComponent(),
				new TranslationTextComponent(titleTranslationKey)
		);
	}
}
