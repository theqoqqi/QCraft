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
	
	private final Map<UUID, BlockPos> map = new HashMap<>();
	
	public LootBoxGeneratorsWorldData(String name) {
		super(name);
	}
	
	@Override
	public void read(@Nonnull CompoundNBT nbt) {
		ListNBT list = nbt.getList("OwnersMap", Constants.NBT.TAG_COMPOUND);
		
		map.clear();
		
		for (int i = 0; i < list.size(); i++) {
			CompoundNBT entry = list.getCompound(i);
			UUID ownerUuid = entry.getUniqueId("OwnerUuid");
			BlockPos blockPos = BlockPos.fromLong(entry.getLong("BlockPos"));
			
			map.put(ownerUuid, blockPos);
		}
	}
	
	@Nonnull
	@Override
	public CompoundNBT write(@Nonnull CompoundNBT nbt) {
		ListNBT list = new ListNBT();
		
		map.forEach((ownerUuid, blockPos) -> {
			CompoundNBT entry = new CompoundNBT();
			
			entry.putUniqueId("OwnerUuid", ownerUuid);
			entry.putLong("BlockPos", blockPos.toLong());
			
			list.add(entry);
		});
		
		nbt.put("OwnersMap", list);
		
		return nbt;
	}
	
	public boolean containsKey(UUID playerUuid) {
		return map.containsKey(playerUuid);
	}
	
	public BlockPos get(UUID playerUuid) {
		return map.get(playerUuid);
	}
	
	public void remove(UUID playerUuid) {
		map.remove(playerUuid);
		markDirty();
	}
	
	public void put(UUID playerUuid, BlockPos pos) {
		map.put(playerUuid, pos);
		markDirty();
	}
	
	public static LootBoxGeneratorsWorldData getInstance(ServerWorld world) {
		return world.getSavedData().getOrCreate(() -> new LootBoxGeneratorsWorldData(NAME), NAME);
	}
}
