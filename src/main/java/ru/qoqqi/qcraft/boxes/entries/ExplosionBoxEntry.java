package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class ExplosionBoxEntry implements IBoxEntry {
	
	private final float power;
	
	public ExplosionBoxEntry(float power) {
		this.power = power;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		double posX = blockPos.getX();
		double posY = blockPos.getY();
		double posZ = blockPos.getZ();
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		result.addChatMessage(getChatMessage(player, lootBox));
		
		level.explode(null, posX, posY, posZ, power, Explosion.BlockInteraction.BREAK);
		
		return result;
	}
	
	protected Component getChatMessage(Player player, ItemStack lootBox) {
		return Component.translatable(
				"lootBox.exploded",
				player.getDisplayName(),
				lootBox.getDisplayName()
		);
	}
}
