package ru.qoqqi.qcraft.boxes.entries;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.boxes.entries.util.IBoxEntry;

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
	public UnpackResult unpack(World world, PlayerEntity player, MinecraftServer server, BlockPos blockPos, ItemStack lootBox) {
		List<ItemStack> generatedLoot = generateLoot(world, player, server);
		
		UnpackResult result = UnpackResult.resultSuccess(lootBox, player);
		fillChatMessages(result, generatedLoot);
		
		generatedLoot.forEach(lootItemStack -> giveOrDropItem(player, lootItemStack));
		
		return result;
	}
	
	protected ResourceLocation getLootTableResourceLocation() {
		return new ResourceLocation(QCraft.MOD_ID, name);
	}
	
	protected List<ItemStack> generateLoot(World world, PlayerEntity player, MinecraftServer server) {
		ResourceLocation location = getLootTableResourceLocation();
		LootTable lootTable = server.getLootTableManager().getLootTableFromLocation(location);
		LootContext.Builder contextBuilder = this.getLootContextBuilder(world, player);
		LootContext lootContext = contextBuilder.build(LootParameterSets.GIFT);
		List<ItemStack> itemStackList = lootTable.generate(lootContext);
		
		if (processor != null) {
			processor.processLootTable(itemStackList, world, player, server);
		}
		
		return itemStackList;
	}
	
	protected void giveOrDropItem(PlayerEntity player, ItemStack itemStack) {
		boolean fullyAdded = player.inventory.addItemStackToInventory(itemStack);
		
		if (fullyAdded && itemStack.isEmpty()) {
			itemStack.setCount(1);
			
			ItemEntity itemEntity = player.dropItem(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.makeFakeItem();
			}
			
			playPickupSound(player);
			player.container.detectAndSendChanges();
			
		} else {
			ItemEntity itemEntity = player.dropItem(itemStack, false);
			
			if (itemEntity != null) {
				itemEntity.setNoPickupDelay();
				itemEntity.setOwnerId(player.getUniqueID());
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
	
	protected ITextComponent getItemReceivedChatMessage(UnpackResult result, ItemStack itemStack) {
		return new TranslationTextComponent(
				"lootBox.itemReceived",
				result.getPlayer().getDisplayName(),
				itemStack.getCount(),
				itemStack.getTextComponent(),
				result.getLootBox().getTextComponent()
		);
	}
	
	protected ITextComponent getGotNothingChatMessage(UnpackResult result) {
		return new TranslationTextComponent(
				"lootBox.gotNothing",
				result.getPlayer().getDisplayName(),
				result.getLootBox().getTextComponent()
		);
	}
	
	protected void playPickupSound(PlayerEntity player) {
		double posX = player.getPosX();
		double posY = player.getPosY();
		double posZ = player.getPosZ();
		SoundEvent sound = SoundEvents.ENTITY_ITEM_PICKUP;
		SoundCategory category = SoundCategory.PLAYERS;
		float volume = 0.2F;
		Random random = player.getRNG();
		float pitch = ((random.nextFloat() - random.nextFloat()) * 0.8F + 1.0F) * 2.0F;
		
		player.world.playSound(null, posX, posY, posZ, sound, category, volume, pitch);
	}
	
	protected LootContext.Builder getLootContextBuilder(World world, PlayerEntity player) {
		return (new LootContext.Builder((ServerWorld) world))
				.withRandom(world.getRandom())
				.withParameter(LootParameters.THIS_ENTITY, player)
				.withParameter(LootParameters.ORIGIN, player.getPositionVec());
	}
	
	public static class UpgradeEnchantmentsProcessor implements LootTableProcessor {
		
		private final RandomValueRange upgradeRange;
		
		public UpgradeEnchantmentsProcessor(int min, int max) {
			this(new RandomValueRange(min, max));
		}
		public UpgradeEnchantmentsProcessor(RandomValueRange upgradeRange) {
			this.upgradeRange = upgradeRange;
		}
		
		@Override
		public void processLootTable(List<ItemStack> lootItems, World world, PlayerEntity player, MinecraftServer server) {
			Random random = world.getRandom();
			
			lootItems.forEach(itemStack -> {
				ListNBT list = itemStack.getEnchantmentTagList();
				for (int i = 0; i < list.size(); i++) {
					CompoundNBT nbt = list.getCompound(i);
					upgradeEnchantment(nbt, random);
				}
			});
		}
		
		private void upgradeEnchantment(CompoundNBT nbt, Random random) {
			String id = nbt.getString("id");
			int level = nbt.getInt("lvl");
			ResourceLocation resourceLocation = ResourceLocation.tryCreate(id);
			Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(resourceLocation);
			
			if (enchantment != null) {
				int maxLevel = enchantment.getMaxLevel();
				
				if (maxLevel > 1) {
					int increasedLevel = level + upgradeRange.generateInt(random);
					nbt.putInt("lvl", Math.min(10, increasedLevel));
				}
			}
		}
	}
	
	public interface LootTableProcessor {
		void processLootTable(List<ItemStack> lootItems, World world, PlayerEntity player, MinecraftServer server);
	}
}
