package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class ExplosionBoxEntry implements IBoxEntry {
	
	private final float power;
	
	public ExplosionBoxEntry(float power) {
		this.power = power;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		double posX = blockPos.getX();
		double posY = blockPos.getY();
		double posZ = blockPos.getZ();
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		result.addChatMessage(getChatMessage(player, lootBox));
		
		world.createExplosion(null, posX, posY, posZ, power, Explosion.Mode.BREAK);
		
		return result;
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox) {
		return new TranslationTextComponent(
				"lootBox.exploded",
				player.getDisplayName(),
				lootBox.getTextComponent()
		);
	}
}
