package ru.qoqqi.qcraft.leveldata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStageState;
import ru.qoqqi.qcraft.journey.JourneyStages;

public class JourneyLevelData extends SavedData {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final String NAME = "Journey";
	
	private static JourneyLevelData loadingInstance = null;
	
	private final Map<JourneyStage, JourneyStageState> stages = new IdentityHashMap<>();
	
	public JourneyLevelData() {
		super();
	}
	
	public JourneyLevelData(@Nonnull CompoundTag nbt) {
		super();
		load(nbt);
	}
	
	public void load(@Nonnull CompoundTag nbt) {
		loadStages(nbt);
	}
	
	private void loadStages(CompoundTag nbt) {
		CompoundTag mapTag = nbt.getCompound("stages");
		
		stages.clear();
		
		LOGGER.info("LOADING PLACES: {}", mapTag.size());
		mapTag.getAllKeys().forEach(stageName -> {
			CompoundTag stateTag = mapTag.getCompound(stageName);
			JourneyStage stage = JourneyStages.byName(stageName);
			JourneyStageState placeData = new JourneyStageState(stateTag);
			
			LOGGER.info("PLACE {}: {} -> {}", stageName, mapTag.get(stageName), placeData.getPosition());
			stages.put(stage, placeData);
		});
	}
	
	@Nonnull
	@Override
	public CompoundTag save(@Nonnull CompoundTag nbt) {
		saveStages(nbt);
		
		return nbt;
	}
	
	private void saveStages(CompoundTag nbt) {
		CompoundTag mapTag = new CompoundTag();
		
		stages.forEach((stage, state) -> {
			mapTag.put(stage.name, state.save(new CompoundTag()));
		});
		
		nbt.put("stages", mapTag);
	}
	
	public boolean shouldGenerate(JourneyStage stage) {
		if (stages.containsKey(stage)) {
			return false;
		}
		
		JourneyStage previous = stage.previous();
		
		return previous == null || isVisited(previous);
	}
	
	@Nullable
	public BlockPos locate(JourneyStage stage) {
		return isGenerated(stage) ? stages.get(stage).getPosition() : null;
	}
	
	public boolean isGenerated(JourneyStage stage) {
		return stages.containsKey(stage) && stages.get(stage).isGenerated();
	}
	
	public boolean isVisited(JourneyStage stage) {
		return isGenerated(stage) && stages.get(stage).isVisited();
	}
	
	public void setFindingPosition(JourneyStage stage) {
		stages.put(stage, new JourneyStageState(JourneyStageState.Status.FINDING_POSITION));
		setDirty();
	}
	
	public void setPiecesPrepared(JourneyStage stage, BlockPos position) {
		JourneyStageState.Status status = JourneyStageState.Status.PIECES_PREPARED;
		stages.put(stage, new JourneyStageState(status, position));
		setDirty();
	}
	
	public void cancelPlacing(JourneyStage stage) {
		stages.remove(stage);
		setDirty();
	}
	
	public void setGenerated(JourneyStage stage, JourneyStageState state) {
		stages.put(stage, state);
		setDirty();
	}
	
	public boolean isVisitedBy(JourneyStage stage, UUID playerUuid) {
		return stages.containsKey(stage) && stages.get(stage).isVisitedBy(playerUuid);
	}
	
	public void addVisitor(JourneyStage stage, UUID playerUuid) {
		if (!stages.containsKey(stage)) {
			return;
		}
		
		stages.get(stage).addVisitor(playerUuid);
		setDirty();
	}
	
	public Map<JourneyStage, BlockPos> getPlacePositions() {
		return stages
				.entrySet()
				.stream()
				.filter(e -> e.getValue().getPosition() != null)
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						e -> e.getValue().getPosition()
				));
	}
	
	public static JourneyLevelData getInstance(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(
				JourneyLevelData::new,
				JourneyLevelData::new,
				NAME
		);
	}
	
	public static void setLoadingInstance(ServerLevel level) {
		loadingInstance = getInstance(level);
	}
	
	public static JourneyLevelData getLoadingInstance() {
		return loadingInstance;
	}
	
	public static class Client {
		
		private static final Set<JourneyStage> visitedPlaces = new HashSet<>();
		
		private static final Map<JourneyStage, BlockPos> placePositions = new HashMap<>();
		
		public static BlockPos getNextPlacePosition() {
			return getPlacePosition(getNextStage());
		}
		
		public static void init() {
			visitedPlaces.clear();
			placePositions.clear();
		}
		
		private static boolean isPlaceVisited(JourneyStage stage) {
			return visitedPlaces.contains(stage);
		}
		
		public static void setPlaceVisited(JourneyStage stage) {
			visitedPlaces.add(stage);
		}
		
		private static BlockPos getPlacePosition(JourneyStage stage) {
			return placePositions.get(stage);
		}
		
		public static void setPlacePosition(JourneyStage stage, BlockPos position) {
			placePositions.put(stage, position);
		}
		
		private static JourneyStage getNextStage() {
			JourneyStage stage = JourneyStages.getFirst();
			
			while (stage != null && isPlaceVisited(stage)) {
				stage = stage.next();
			}
			
			return stage == null ? JourneyStages.getLast() : stage;
		}
	}
}
