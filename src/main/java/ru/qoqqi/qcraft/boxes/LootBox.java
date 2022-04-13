package ru.qoqqi.qcraft.boxes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.WeightedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RandomBoxEntry;
import ru.qoqqi.qcraft.boxes.entries.util.RetryForSuccessBoxEntry;

public class LootBox {

	private final WeightedList<IBoxEntry> entries;
	
	public LootBox(WeightedList<IBoxEntry> entries) {
		this.entries = entries;
	}
	
	public WeightedList<IBoxEntry> getEntries() {
		return entries;
	}
	
	@Nonnull
	public ActionResultType openWithActionResult(PlayerEntity player, ItemStack itemStack, BlockPos blockPos) {
		IBoxEntry.UnpackResult result = open(player, itemStack, blockPos, true);
		
		return result.isSuccessful() ? ActionResultType.CONSUME : ActionResultType.FAIL;
	}
	
	@Nonnull
	public IBoxEntry.UnpackResult open(PlayerEntity player, ItemStack itemStack, BlockPos blockPos) {
		return open(player, itemStack, blockPos, false);
	}
	
	@Nonnull
	private IBoxEntry.UnpackResult open(PlayerEntity player, ItemStack itemStack, BlockPos blockPos, boolean broadcastResult) {
		World world = player.getEntityWorld();
		MinecraftServer server = world.getServer();

		if (server == null) {
			return IBoxEntry.UnpackResult.resultFail(itemStack, player);
		}

		IBoxEntry randomEntry = new RetryForSuccessBoxEntry(10, new RandomBoxEntry(getEntries()));
		IBoxEntry.UnpackResult result = randomEntry.unpack(world, player, server, blockPos, itemStack);

		if (broadcastResult) {
			broadcastResult(player, server, itemStack, result);
		}

		return result;
	}
	
	private static void broadcastResult(PlayerEntity player, MinecraftServer server, ItemStack lootBox, IBoxEntry.UnpackResult result) {
		if (result.isSuccessful()) {
			for (ITextComponent chatMessage : result.getChatMessages()) {
				sendToAllPlayers(server, player, chatMessage);
			}
		} else {
			ITextComponent chatMessage = getFailureChatMessage(player, lootBox);
			sendToAllPlayers(server, player, chatMessage);
		}
	}
	
	private static ITextComponent getFailureChatMessage(PlayerEntity player, ItemStack lootBox) {
		return new TranslationTextComponent(
				"lootBox.failure",
				player.getDisplayName(),
				lootBox.getTextComponent()
		);
	}
	
	private static void sendToAllPlayers(MinecraftServer server, PlayerEntity player, ITextComponent chatMessage) {
		server.getPlayerList().sendMessageToTeamOrAllPlayers(player, chatMessage);
	}
}
