package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class ExperienceBoxEntry implements IBoxEntry {
	
	private final int average;
	
	private final int spread;
	
	public ExperienceBoxEntry(int average, int spread) {
		this.average = average;
		this.spread = spread + 1;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		int experience = average + world.rand.nextInt(spread) - world.rand.nextInt(spread);
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		result.addChatMessage(getChatMessage(player, lootBox, experience));
		
		while (experience > 0) {
			int orbExperience = ExperienceOrbEntity.getXPSplit(experience > 100 ? experience / 10 : experience);
			experience -= orbExperience;
			
			double posX = blockPos.getX() + world.rand.nextFloat() - world.rand.nextFloat();
			double posY = blockPos.getY() + world.rand.nextFloat() - world.rand.nextFloat();
			double posZ = blockPos.getZ() + world.rand.nextFloat() - world.rand.nextFloat();
			
			world.addEntity(new ExperienceOrbEntity(world, posX, posY, posZ, orbExperience));
		}
		
		return result;
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox, int experience) {
		return new TranslationTextComponent(
				"lootBox.gotExperience",
				player.getDisplayName(),
				lootBox.getTextComponent(),
				experience
		);
	}
}
