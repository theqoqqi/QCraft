package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class TeleportBoxEntry implements IBoxEntry {
	
	private final BiFunction<PlayerEntity, ServerWorld, BlockPos> positionFunction;
	
	private final Supplier<ITextComponent> locationTextComponentSupplier;
	
	public TeleportBoxEntry(BiFunction<PlayerEntity, ServerWorld, BlockPos> positionFunction, Supplier<ITextComponent> locationTextComponentSupplier) {
		this.positionFunction = positionFunction;
		this.locationTextComponentSupplier = locationTextComponentSupplier;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		BlockPos position = positionFunction.apply(player, (ServerWorld) world);
		
		player.setPosition(position.getX(), position.getY(), position.getZ());
		
		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox, locationTextComponentSupplier.get()));
	}
	
	protected ITextComponent getChatMessage(PlayerEntity player, ItemStack lootBox, ITextComponent locationTextComponent) {
		return new TranslationTextComponent(
				"lootBox.teleport",
				player.getDisplayName(),
				lootBox.getTextComponent(),
				locationTextComponent
		);
	}
}
