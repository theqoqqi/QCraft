package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		int experience = average + level.random.nextInt(spread) - level.random.nextInt(spread);
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		result.addChatMessage(getChatMessage(player, lootBox, experience));
		
		while (experience > 0) {
			int orbExperience = ExperienceOrb.getExperienceValue(experience > 100 ? experience / 10 : experience);
			experience -= orbExperience;
			
			double posX = blockPos.getX() + level.random.nextFloat() - level.random.nextFloat();
			double posY = blockPos.getY() + level.random.nextFloat() - level.random.nextFloat();
			double posZ = blockPos.getZ() + level.random.nextFloat() - level.random.nextFloat();
			
			level.addFreshEntity(new ExperienceOrb(level, posX, posY, posZ, orbExperience));
		}
		
		return result;
	}
	
	protected Component getChatMessage(Player player, ItemStack lootBox, int experience) {
		return Component.translatable(
				"lootBox.gotExperience",
				player.getDisplayName(),
				lootBox.getDisplayName(),
				experience
		);
	}
}
