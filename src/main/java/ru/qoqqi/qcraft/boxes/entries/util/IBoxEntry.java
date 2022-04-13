package ru.qoqqi.qcraft.boxes.entries.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public interface IBoxEntry {

	@Nonnull
	UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox);
	
	class UnpackResult {
		
		protected final PlayerEntity player;
		
		protected final ItemStack lootBox;
		
		protected final boolean isSuccessful;
		
		protected List<ITextComponent> chatMessages = new ArrayList<>();
		
		private UnpackResult(ItemStack lootBox, PlayerEntity player, boolean isSuccessful) {
			this.player = player;
			this.lootBox = lootBox;
			this.isSuccessful = isSuccessful;
		}
		
		public boolean isSuccessful() {
			return isSuccessful;
		}
		
		public void addChatMessage(ITextComponent chatMessage) {
			this.chatMessages.add(chatMessage);
		}
		
		public UnpackResult withChatMessage(ITextComponent chatMessage) {
			addChatMessage(chatMessage);
			return this;
		}
		
		public List<ITextComponent> getChatMessages() {
			return chatMessages;
		}
		
		public PlayerEntity getPlayer() {
			return player;
		}
		
		public ItemStack getLootBox() {
			return lootBox;
		}
		
		public void merge(UnpackResult otherResult) {
			otherResult.getChatMessages().forEach(this::addChatMessage);
		}
		
		public static UnpackResult resultSuccess(ItemStack lootBox, PlayerEntity player) {
			return new UnpackResult(lootBox, player, true);
		}
		
		public static UnpackResult resultFail(ItemStack lootBox, PlayerEntity player) {
			return new UnpackResult(lootBox, player, false);
		}
		
		public static void mergeChatMessages(UnpackResult toResult, Collection<UnpackResult> fromResults) {
			fromResults.forEach(toResult::merge);
		}
	}
}
