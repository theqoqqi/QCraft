package ru.qoqqi.qcraft.leveldata;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;

public class LootBoxGeneratorsLevelData extends SavedData {
	
	public static final String NAME = "LootBoxGenerators";
	
	private final Map<UUID, BlockPos> activatedBlocks = new HashMap<>();
	
	private final Map<UUID, Integer> generationDurations = new HashMap<>();
	
	public LootBoxGeneratorsLevelData() {
		super();
	}
	
	public LootBoxGeneratorsLevelData(@Nonnull CompoundTag nbt) {
		super();
		read(nbt);
	}
	
	public void read(@Nonnull CompoundTag nbt) {
		readOwnersMap(nbt);
		readDurationsMap(nbt);
	}
	
	private void readOwnersMap(@Nonnull CompoundTag nbt) {
		ListTag list = nbt.getList("OwnersMap", Tag.TAG_COMPOUND);
		
		activatedBlocks.clear();
		
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			UUID ownerUuid = entry.getUUID("OwnerUuid");
			BlockPos blockPos = BlockPos.of(entry.getLong("BlockPos"));
			
			activatedBlocks.put(ownerUuid, blockPos);
		}
	}
	
	private void readDurationsMap(@Nonnull CompoundTag nbt) {
		ListTag list = nbt.getList("DurationsMap", Tag.TAG_COMPOUND);
		
		generationDurations.clear();
		
		for (int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			UUID ownerUuid = entry.getUUID("PlayerUuid");
			Integer duration = entry.getInt("Duration");
			
			generationDurations.put(ownerUuid, duration);
		}
	}
	
	@Nonnull
	@Override
	public CompoundTag save(@Nonnull CompoundTag nbt) {
		writeOwnersMap(nbt);
		writeDurationsMap(nbt);
		
		return nbt;
	}
	
	private void writeOwnersMap(@Nonnull CompoundTag nbt) {
		ListTag list = new ListTag();
		
		activatedBlocks.forEach((ownerUuid, blockPos) -> {
			CompoundTag entry = new CompoundTag();
			
			entry.putUUID("OwnerUuid", ownerUuid);
			entry.putLong("BlockPos", blockPos.asLong());
			
			list.add(entry);
		});
		
		nbt.put("OwnersMap", list);
	}
	
	private void writeDurationsMap(@Nonnull CompoundTag nbt) {
		ListTag list = new ListTag();
		
		generationDurations.forEach((ownerUuid, duration) -> {
			CompoundTag entry = new CompoundTag();
			
			entry.putUUID("PlayerUuid", ownerUuid);
			entry.putInt("Duration", duration);
			
			list.add(entry);
		});
		
		nbt.put("DurationsMap", list);
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean hasActivatedBlock(UUID playerUuid) {
		return activatedBlocks.containsKey(playerUuid);
	}
	
	public BlockPos getActivatedBlock(UUID playerUuid) {
		return activatedBlocks.get(playerUuid);
	}
	
	public void removeActivatedBlock(UUID playerUuid) {
		activatedBlocks.remove(playerUuid);
		setDirty();
	}
	
	public void setActivatedBlock(UUID playerUuid, BlockPos pos) {
		activatedBlocks.put(playerUuid, pos);
		setDirty();
	}
	
	public boolean hasGenerationDuration(UUID playerUuid) {
		return generationDurations.containsKey(playerUuid);
	}
	
	public Integer getGenerationDuration(UUID playerUuid) {
		return generationDurations.get(playerUuid);
	}
	
	public void removeGenerationDuration(UUID playerUuid) {
		generationDurations.remove(playerUuid);
		setDirty();
	}
	
	public void setGenerationDuration(UUID playerUuid, Integer duration) {
		generationDurations.put(playerUuid, duration);
		setDirty();
	}
	
	public static LootBoxGeneratorsLevelData getInstance(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				LootBoxGeneratorsLevelData::new,
				LootBoxGeneratorsLevelData::new,
				NAME
		);
	}
}
