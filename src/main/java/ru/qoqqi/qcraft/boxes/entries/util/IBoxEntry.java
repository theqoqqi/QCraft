package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public interface IBoxEntry {

	@Nonnull
	UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox);
	
	class UnpackResult {
		
		protected final Player player;
		
		protected final ItemStack lootBox;
		
		protected final boolean isSuccessful;
		
		protected List<Component> chatMessages = new ArrayList<>();
		
		private UnpackResult(ItemStack lootBox, Player player, boolean isSuccessful) {
			this.player = player;
			this.lootBox = lootBox;
			this.isSuccessful = isSuccessful;
		}
		
		public boolean isSuccessful() {
			return isSuccessful;
		}
		
		public void addChatMessage(Component chatMessage) {
			this.chatMessages.add(chatMessage);
		}
		
		public UnpackResult withChatMessage(Component chatMessage) {
			addChatMessage(chatMessage);
			return this;
		}
		
		public List<Component> getChatMessages() {
			return chatMessages;
		}
		
		public Player getPlayer() {
			return player;
		}
		
		public ItemStack getLootBox() {
			return lootBox;
		}
		
		public void merge(UnpackResult otherResult) {
			otherResult.getChatMessages().forEach(this::addChatMessage);
		}
		
		public static UnpackResult resultSuccess(ItemStack lootBox, Player player) {
			return new UnpackResult(lootBox, player, true);
		}
		
		public static UnpackResult resultFail(ItemStack lootBox, Player player) {
			return new UnpackResult(lootBox, player, false);
		}
		
		public static void mergeChatMessages(UnpackResult toResult, Collection<UnpackResult> fromResults) {
			fromResults.forEach(toResult::merge);
		}
	}
}
