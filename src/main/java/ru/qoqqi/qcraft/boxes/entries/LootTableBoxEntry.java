package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;
import ru.qoqqi.qcraft.util.IntRange;

public class LootTableBoxEntry implements IBoxEntry {
	
	protected final String name;
	
	private LootTableProcessor processor;
	
	public LootTableBoxEntry(String name) {
		this.name = name;
	}
	
	public LootTableBoxEntry withProcessor(LootTableProcessor processor) {
		this.processor = processor;
		return this;
	}
	
	@Nonnull
	@Override
	public UnpackResult unpack(Level level, Player player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		List<ItemStack> generatedLoot = generateLoot(level, player, server);
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		fillChatMessages(result, generatedLoot);
		
		generatedLoot.forEach(lootItemStack -> giveOrDropItem(player, lootItemStack));
		
		return result;
	}
	
	protected ResourceLocation getLootTableResourceLocation() {
		return new ResourceLocation(QCraft.MOD_ID, name);
	}
	
	protected List<ItemStack> generateLoot(Level level, Player player, MinecraftServer server) {
		ResourceLocation location = getLootTableResourceLocation();
		LootTable lootTable = server.getLootTables().get(location);
		LootContext.Builder contextBuilder = this.getLootContextBuilder(level, player);
		LootContext lootContext = contextBuilder.create(LootContextParamSets.GIFT);
		List<ItemStack> itemStackList = lootTable.getRandomItems(lootContext);
		
		if (processor != null) {
			processor.processLootTable(itemStackList, level, player, server);
		}
		
		return itemStackList;
	}
	
	protected void giveOrDropItem(Player player, ItemStack itemStack) {
		boolean fullyAdded = player.getInventory().add(itemStack);
		
		if (fullyAdded && itemStack.isEmpty()) {
			itemStack.setCount(1);
			
			ItemEntity itemEntity = player.drop(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.makeFakeItem();
			}
			
			playPickupSound(player);
			player.containerMenu.broadcastChanges();
			
		} else {
			ItemEntity itemEntity = player.drop(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.setNoPickUpDelay();
				itemEntity.setOwner(player.getUUID());
			}
		}
	}
	
	protected void fillChatMessages(UnpackResult result, List<ItemStack> lootContent) {
		if (lootContent.isEmpty()) {
			result.addChatMessage(getGotNothingChatMessage(result));
			return;
		}
		
		lootContent.forEach(itemStack -> {
			result.addChatMessage(getItemReceivedChatMessage(result, itemStack));
		});
	}
	
	protected Component getItemReceivedChatMessage(UnpackResult result, ItemStack itemStack) {
		return Component.translatable(
				"lootBox.itemReceived",
				result.getPlayer().getDisplayName(),
				itemStack.getCount(),
				itemStack.getDisplayName(),
				result.getLootBox().getDisplayName()
		);
	}
	
	protected Component getGotNothingChatMessage(UnpackResult result) {
		return Component.translatable(
				"lootBox.gotNothing",
				result.getPlayer().getDisplayName(),
				result.getLootBox().getDisplayName()
		);
	}
	
	protected void playPickupSound(Player player) {
		double posX = player.getX();
		double posY = player.getY();
		double posZ = player.getZ();
		SoundEvent sound = SoundEvents.ITEM_PICKUP;
		SoundSource category = SoundSource.PLAYERS;
		float volume = 0.2F;
		RandomSource random = player.getRandom();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.8F + 1.0F) * 2.0F;
		
		player.level.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
	}
	
	protected LootContext.Builder getLootContextBuilder(Level level, Player player) {
		return (new LootContext.Builder((ServerLevel) level))
				.withRandom(level.getRandom())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, player.getPosition(0));
	}
	
	public static class UpgradeEnchantmentsProcessor implements LootTableProcessor {
		
		private final IntRange upgradeRange;
		
		public UpgradeEnchantmentsProcessor(int min, int max) {
			this.upgradeRange = IntRange.of(min, max);
		}
		
		@Override
		public void processLootTable(List<ItemStack> lootItems, Level level, Player player, MinecraftServer server) {
			RandomSource random = level.getRandom();
			
			lootItems.forEach(itemStack -> {
				ListTag list = itemStack.getEnchantmentTags();
				for (int i = 0; i < list.size(); i++) {
					CompoundTag nbt = list.getCompound(i);
					upgradeEnchantment(nbt, random);
				}
			});
		}
		
		private void upgradeEnchantment(CompoundTag nbt, RandomSource random) {
			String id = nbt.getString("id");
			int level = nbt.getInt("lvl");
			ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
			Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
			
			if (enchantment != null) {
				int maxLevel = enchantment.getMaxLevel();
				
				if (maxLevel > 1) {
					int increasedLevel = level + upgradeRange.getRandomValue(random);
					nbt.putInt("lvl", Math.min(10, increasedLevel));
				}
			}
		}
	}
	
	public interface LootTableProcessor {
		void processLootTable(List<ItemStack> lootItems, Level level, Player player, MinecraftServer server);
	}
}
