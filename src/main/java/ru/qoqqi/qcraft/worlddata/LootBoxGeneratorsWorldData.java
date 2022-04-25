package ru.qoqqi.qcraft.worlddata;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

public class LootBoxGeneratorsWorldData extends WorldSavedData {
	
	public static final String NAME = "LootBoxGenerators";
	
	private final Map<UUID, BlockPos> activatedBlocks = new HashMap<>();
	
	private final Map<UUID, Integer> generationDurations = new HashMap<>();
	
	public LootBoxGeneratorsWorldData(String name) {
		super(name);
	}
	
	@Override
	public void read(@Nonnull CompoundNBT nbt) {
		readOwnersMap(nbt);
		readDurationsMap(nbt);
	}
	
	private void readOwnersMap(@Nonnull CompoundNBT nbt) {
		ListNBT list = nbt.getList("OwnersMap", Constants.NBT.TAG_COMPOUND);
		
		activatedBlocks.clear();
		
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT entry = list.getCompound(i);
			UUID ownerUuid = entry.getUniqueId("OwnerUuid");
			BlockPos blockPos = BlockPos.fromLong(entry.getLong("BlockPos"));
			
			activatedBlocks.put(ownerUuid, blockPos);
		}
	}
	
	private void readDurationsMap(@Nonnull CompoundNBT nbt) {
		ListNBT list = nbt.getList("DurationsMap", Constants.NBT.TAG_COMPOUND);
		
		generationDurations.clear();
		
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT entry = list.getCompound(i);
			UUID ownerUuid = entry.getUniqueId("PlayerUuid");
			Integer duration = entry.getInt("Duration");
			
			generationDurations.put(ownerUuid, duration);
		}
	}
	
	@Nonnull
	@Override
	public CompoundNBT write(@Nonnull CompoundNBT nbt) {
		writeOwnersMap(nbt);
		writeDurationsMap(nbt);
		
		return nbt;
	}
	
	private void writeOwnersMap(@Nonnull CompoundNBT nbt) {
		ListNBT list = new ListNBT();
		
		activatedBlocks.forEach((ownerUuid, blockPos) -> {
			CompoundNBT entry = new CompoundNBT();
			
			entry.putUniqueId("OwnerUuid", ownerUuid);
			entry.putLong("BlockPos", blockPos.toLong());
			
			list.add(entry);
		});
		
		nbt.put("OwnersMap", list);
	}
	
	private void writeDurationsMap(@Nonnull CompoundNBT nbt) {
		ListNBT list = new ListNBT();
		
		generationDurations.forEach((ownerUuid, duration) -> {
			CompoundNBT entry = new CompoundNBT();
			
			entry.putUniqueId("PlayerUuid", ownerUuid);
			entry.putInt("Duration", duration);
			
			list.add(entry);
		});
		
		nbt.put("DurationsMap", list);
	}
	
	public boolean hasActivatedBlock(UUID playerUuid) {
		return activatedBlocks.containsKey(playerUuid);
	}
	
	public BlockPos getActivatedBlock(UUID playerUuid) {
		return activatedBlocks.get(playerUuid);
	}
	
	public void removeActivatedBlock(UUID playerUuid) {
		activatedBlocks.remove(playerUuid);
		markDirty();
	}
	
	public void setActivatedBlock(UUID playerUuid, BlockPos pos) {
		activatedBlocks.put(playerUuid, pos);
		markDirty();
	}
	
	public boolean hasGenerationDuration(UUID playerUuid) {
		return generationDurations.containsKey(playerUuid);
	}
	
	public Integer getGenerationDuration(UUID playerUuid) {
		return generationDurations.get(playerUuid);
	}
	
	public void removeGenerationDuration(UUID playerUuid) {
		generationDurations.remove(playerUuid);
		markDirty();
	}
	
	public void setGenerationDuration(UUID playerUuid, Integer duration) {
		generationDurations.put(playerUuid, duration);
		markDirty();
	}
	
	public static LootBoxGeneratorsWorldData getInstance(ServerWorld world) {
		return world.getSavedData().getOrCreate(() -> new LootBoxGeneratorsWorldData(NAME), NAME);
	}
}
