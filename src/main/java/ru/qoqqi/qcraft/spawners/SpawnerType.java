package ru.qoqqi.qcraft.spawners;

import com.mojang.serialization.Codec;

import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import ru.qoqqi.qcraft.spawners.random.GroundSpawnPosSupplier;
import ru.qoqqi.qcraft.spawners.random.UndergroundBlockPosSupplier;

public enum SpawnerType implements StringRepresentable {
	
	GROUND_CREATURE("ground_creature", true, true, spawnerType -> {
		return new ChunkGenerationSpawnerTypeHandler(spawnerType, 0.002f, GroundSpawnPosSupplier::inChunk);
	}),
	UNDERGROUND_CREATURE("underground_creature", true, true, spawnerType -> {
		return new ChunkGenerationSpawnerTypeHandler(spawnerType, 0.1f, UndergroundBlockPosSupplier::inChunk);
	}),
	WATER_CREATURE("water_creature", true, true, spawnerType -> {
		return new ChunkGenerationSpawnerTypeHandler(spawnerType, 0.01f, GroundSpawnPosSupplier::inChunk);
	});
	
	public static final Codec<SpawnerType> CODEC =
			StringRepresentable.fromEnum(SpawnerType::values);
	
	private static final java.util.Map<String, SpawnerType> BY_NAME = Arrays.stream(values())
			.collect(Collectors.toMap(SpawnerType::getSerializedName, spawnerType -> spawnerType));
	
	public final String name;
	
	public final boolean isFriendly;
	
	public final boolean spawnsOnChunkGeneration;
	
	public final Function<SpawnerType, SpawnerTypeHandler> spawnerTypeHandlerFactory;
	
	SpawnerType(String name, boolean isFriendly, boolean spawnsOnChunkGeneration,
	            Function<SpawnerType, SpawnerTypeHandler> spawnerTypeHandlerFactory) {
		
		this.name = name;
		this.isFriendly = isFriendly;
		this.spawnsOnChunkGeneration = spawnsOnChunkGeneration;
		this.spawnerTypeHandlerFactory = spawnerTypeHandlerFactory;
	}
	
	@Override
	@NotNull
	public String getSerializedName() {
		return name;
	}
	
	public static SpawnerType byName(String name) {
		return BY_NAME.get(name);
	}
}
