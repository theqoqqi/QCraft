package ru.qoqqi.qcraft.boxes;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RandomBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RetryForSuccessBoxEntry;
import ru.qoqqi.qcraft.util.WeightedList;

public class LootBox {

	private final WeightedList<IBoxEntry> entries;
	
	public LootBox(WeightedList<IBoxEntry> entries) {
		this.entries = entries;
	}
	
	public WeightedList<IBoxEntry> getEntries() {
		return entries;
	}

	@Nonnull
	public InteractionResult openWithActionResult(Player player, ItemStack itemStack, BlockPos blockPos) {
		IBoxEntry.UnpackResult result = open(player, itemStack, blockPos, true);

		return result.isSuccessful() ? InteractionResult.CONSUME : InteractionResult.FAIL;
	}
	
	@Nonnull
	public IBoxEntry.UnpackResult openSilently(Player player, BlockPos blockPos) {
		return openSilently(player, ItemStack.EMPTY, blockPos);
	}
	
	@Nonnull
	public IBoxEntry.UnpackResult openSilently(Player player, ItemStack itemStack, BlockPos blockPos) {
		return open(player, itemStack, blockPos, false);
	}
	
	@Nonnull
	private IBoxEntry.UnpackResult open(Player player, ItemStack itemStack, BlockPos blockPos, boolean broadcastResult) {
		Level level = player.level();
		MinecraftServer server = level.getServer();

		if (server == null) {
			return IBoxEntry.UnpackResult.resultFail(itemStack, player);
		}

		IBoxEntry randomEntry = new RetryForSuccessBoxEntry(10, new RandomBoxEntry(getEntries()));
		IBoxEntry.UnpackResult result = randomEntry.unpack(level, player, server, blockPos, itemStack);

		if (broadcastResult) {
			broadcastResult(player, server, itemStack, result);
		}

		return result;
	}
	
	private static void broadcastResult(Player player, MinecraftServer server, ItemStack lootBox, IBoxEntry.UnpackResult result) {
		if (result.isSuccessful()) {
			for (Component chatMessage : result.getChatMessages()) {
				sendToAllPlayers(server, player, chatMessage);
			}
		} else {
			Component chatMessage = getFailureChatMessage(player, lootBox);
			sendToAllPlayers(server, player, chatMessage);
		}
	}
	
	private static Component getFailureChatMessage(Player player, ItemStack lootBox) {
		return Component.translatable(
				"lootBox.failure",
				player.getDisplayName(),
				lootBox.getDisplayName()
		);
	}
	
	private static void sendToAllPlayers(MinecraftServer server, Player player, Component chatMessage) {
		server.getPlayerList().broadcastSystemToAllExceptTeam(player, chatMessage);
	}
}
