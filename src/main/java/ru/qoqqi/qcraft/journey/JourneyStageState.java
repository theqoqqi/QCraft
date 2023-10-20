package ru.qoqqi.qcraft.journey;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

public class JourneyStageState {
	
	private Status status;
	
	private BlockPos position;
	
	private final List<UUID> visitors = new ArrayList<>();
	
	public JourneyStageState(Status status) {
		this.status = status;
	}
	
	public JourneyStageState(Status status, BlockPos position) {
		this.status = status;
		this.position = position;
	}
	
	public JourneyStageState(BlockPos position) {
		this.status = Status.GENERATED;
		this.position = position;
	}
	
	public JourneyStageState(CompoundTag nbt) {
		load(nbt);
	}
	
	public void load(@Nonnull CompoundTag nbt) {
		this.status = Status.valueOf(nbt.getString("status"));
		
		if (nbt.contains("position", Tag.TAG_COMPOUND)) {
			position = NbtUtils.readBlockPos(nbt.getCompound("position"));
		}
		
		loadVisitors(nbt);
	}
	
	private void loadVisitors(CompoundTag nbt) {
		ListTag listTag = nbt.getList("visitors", Tag.TAG_INT_ARRAY);
		
		visitors.clear();
		
		for (Tag tag : listTag) {
			visitors.add(NbtUtils.loadUUID(tag));
		}
	}
	
	public CompoundTag save(@Nonnull CompoundTag nbt) {
		nbt.putString("status", status.name());
		
		if (position != null) {
			nbt.put("position", NbtUtils.writeBlockPos(position));
		}
		
		saveVisitors(nbt);
		
		return nbt;
	}
	
	private void saveVisitors(CompoundTag nbt) {
		ListTag listTag = new ListTag();
		
		visitors.forEach(visitor -> {
			listTag.add(NbtUtils.createUUID(visitor));
		});
		
		nbt.put("visitors", listTag);
	}
	
	public BlockPos getPosition() {
		return position;
	}
	
	public boolean isGenerated() {
		return status == Status.GENERATED;
	}
	
	public boolean isVisited() {
		return !visitors.isEmpty();
	}
	
	public boolean isVisitedBy(UUID visitor) {
		return visitors.contains(visitor);
	}
	
	public void addVisitor(UUID visitor) {
		visitors.add(visitor);
	}
	
	public enum Status {
		FINDING_POSITION,
		PIECES_PREPARED,
		GENERATED
	}
}
