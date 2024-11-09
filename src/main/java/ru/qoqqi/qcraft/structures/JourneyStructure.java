package ru.qoqqi.qcraft.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraftforge.network.PacketDistributor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.config.Config;
import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStageState;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;
import ru.qoqqi.qcraft.network.JourneyPlacePositionPacket;
import ru.qoqqi.qcraft.network.ModPacketHandler;

public class JourneyStructure extends Structure {

	private static final Logger LOGGER = LogManager.getLogger();

	public static final Codec<JourneyStructure> CODEC = RecordCodecBuilder.create(builder -> builder
			.group(
					Codec.STRING.fieldOf("stage").forGetter(s -> s.stage.name),
					CustomJigsawStructure.CODEC.fieldOf("jigsaw_options").forGetter(s -> s.jigsawStructure)
			)
			.apply(builder, JourneyStructure::new)
	);

	private final JourneyStage stage;

	private final CustomJigsawStructure jigsawStructure;

	public JourneyStructure(String stageName, CustomJigsawStructure jigsawStructure) {
		super(jigsawStructure.getModifiedStructureSettings());
		this.stage = JourneyStages.byName(stageName);
		this.jigsawStructure = jigsawStructure;
	}

	@NotNull
	public Optional<GenerationStub> findGenerationPoint(@NotNull GenerationContext context) {
		if (!Config.COMMON.journeyEnabled.get()) {
			return Optional.empty();
		}

		JourneyLevelData levelData = JourneyLevelData.getLoadingInstance();

		if (!levelData.shouldGenerate(stage)) {
//			LOGGER.info("SKIPPED: {}", stage.name);
			BlockPos position = levelData.locate(stage);

			return position == null
					? Optional.empty()
					: Optional.of(new GenerationStub(position, builder -> {
			}));
		}

//		LOGGER.info("FINDING POSITION: {} in chunk {}", stage.name, context.chunkPos());
		levelData.setFindingPosition(stage);
		Optional<GenerationStub> result = jigsawStructure.findGenerationPoint(context);

		boolean willBeGenerated = willBeGenerated(result.orElse(null), context);

		if (willBeGenerated) {
			BlockPos position = context.chunkPos().getWorldPosition();
			MinecraftServer server = QCraft.getLastStartedServer();

			levelData.setPiecesPrepared(stage, position);
			sendPlacePositionToPlayers(server, position);
			LOGGER.info("PIECES PREPARED: {}", stage.name);
		} else {
			levelData.cancelPlacing(stage);
//			LOGGER.info("CANCELLED: {}", stage.name);
		}

		return result;
	}

	private static boolean willBeGenerated(@Nullable GenerationStub result, @NotNull GenerationContext context) {
		return result != null
				&& !result.getPiecesBuilder().isEmpty()
				&& isValidBiome(result, context);
	}

	private static boolean isValidBiome(@NotNull GenerationStub result, @NotNull GenerationContext context) {
		ChunkGenerator chunkGenerator = context.chunkGenerator();
		RandomState randomState = context.randomState();
		Predicate<Holder<Biome>> validBiome = context.validBiome();

		BlockPos blockpos = result.position();
		Climate.Sampler sampler = randomState.sampler();

		int pX = QuartPos.fromBlock(blockpos.getX());
		int pY = QuartPos.fromBlock(blockpos.getY());
		int pZ = QuartPos.fromBlock(blockpos.getZ());

		Holder<Biome> noiseBiome = chunkGenerator.getBiomeSource()
				.getNoiseBiome(pX, pY, pZ, sampler);

		return validBiome.test(noiseBiome);
	}

	@Override
	public void afterPlace(@NotNull WorldGenLevel level,
	                       @NotNull StructureManager structureManager,
	                       @NotNull ChunkGenerator chunkGenerator,
	                       @NotNull RandomSource randomSource,
	                       @NotNull BoundingBox boundingBox,
	                       @NotNull ChunkPos chunkPos,
	                       @NotNull PiecesContainer piecesContainer) {

		super.afterPlace(level, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, piecesContainer);

		JourneyLevelData levelData = JourneyLevelData.getLoadingInstance();

		if (levelData.isGenerated(stage)) {
//			LOGGER.info("GENERATED SKIPPED: {}", stage.name);
			return;
		}

		BlockPos position = getStructureCenter(boundingBox, piecesContainer);
		JourneyStageState stageState = new JourneyStageState(position);
		MinecraftServer server = level.getServer();

		levelData.setGenerated(stage, stageState);
		sendPlacePositionToPlayers(server, position);

		LOGGER.info("GENERATED: {} {}", stage.name, position);
	}

	private void sendPlacePositionToPlayers(@Nullable MinecraftServer server, BlockPos position) {
		JourneyPlacePositionPacket packet = new JourneyPlacePositionPacket(stage, position);

		if (server != null) {
			server.getPlayerList().getPlayers().forEach(player -> {
				ModPacketHandler.CHANNEL.send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
			});
		}
	}

	private static BlockPos getStructureCenter(BoundingBox boundingBox, PiecesContainer piecesContainer) {
		List<StructurePiece> pieces = piecesContainer.pieces();

		if (pieces.isEmpty()) {
			return boundingBox.getCenter();
		}

		StructurePiece piece = pieces.get(0);

		return piece.getBoundingBox().getCenter();
	}

	@NotNull
	public StructureType<?> type() {
		return ModStructureTypes.JOURNEY_PLACE.get();
	}
}
