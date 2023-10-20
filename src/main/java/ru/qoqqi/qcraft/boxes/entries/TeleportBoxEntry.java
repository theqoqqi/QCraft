package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

public class TeleportBoxEntry implements IBoxEntry {

	private final BiFunction<Player, ServerLevel, BlockPos> positionFunction;

	private final Supplier<Component> locationTextComponentSupplier;

	public TeleportBoxEntry(BiFunction<Player, ServerLevel, BlockPos> positionFunction, Supplier<Component> locationTextComponentSupplier) {
		this.positionFunction = positionFunction;
		this.locationTextComponentSupplier = locationTextComponentSupplier;
	}

	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		BlockPos position = positionFunction.apply(player, (ServerLevel) level);

		player.setPos(position.getX(), position.getY(), position.getZ());

		return UnpackResult.resultSuccess(lootBox, player)
				.withChatMessage(getChatMessage(player, lootBox, locationTextComponentSupplier.get()));
	}

	protected Component getChatMessage(Player player, ItemStack lootBox, Component locationTextComponent) {
		return Component.translatable(
				"lootBox.teleport",
				player.getDisplayName(),
				lootBox.getDisplayName(),
				locationTextComponent
		);
	}
}
