package ru.qoqqi.qcraft.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class CustomJigsawStructure extends Structure {
	
	public static final int MAX_TOTAL_STRUCTURE_RANGE = 128;
	
	public static final Codec<CustomJigsawStructure> CODEC = RecordCodecBuilder.<CustomJigsawStructure>mapCodec(builder -> builder
			.group(
					settingsCodec(builder),
					StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((s) -> s.startPool),
					ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((s) -> s.startJigsawName),
					Codec.intRange(0, 10).fieldOf("size").forGetter((s) -> s.maxDepth),
					HeightProvider.CODEC.fieldOf("start_height").forGetter((s) -> s.startHeight),
					Codec.BOOL.fieldOf("use_expansion_hack").forGetter((s) -> s.useExpansionHack),
					Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((s) -> s.projectStartToHeightmap),
					Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter((s) -> s.maxDistanceFromCenter)
			)
			.apply(builder, CustomJigsawStructure::new)
	).flatXmap(verifyRange(), verifyRange()).codec();
	
	private final Holder<StructureTemplatePool> startPool;
	private final Optional<ResourceLocation> startJigsawName;
	private final int maxDepth;
	private final HeightProvider startHeight;
	private final boolean useExpansionHack;
	private final Optional<Heightmap.Types> projectStartToHeightmap;
	private final int maxDistanceFromCenter;
	
	private static Function<CustomJigsawStructure, DataResult<CustomJigsawStructure>> verifyRange() {
		return (p_227638_) -> {
			byte b0;
			switch (p_227638_.terrainAdaptation()) {
				case NONE:
					b0 = 0;
					break;
				case BURY:
				case BEARD_THIN:
				case BEARD_BOX:
					b0 = 12;
					break;
				default:
					throw new IncompatibleClassChangeError();
			}
			
			int i = b0;
			return p_227638_.maxDistanceFromCenter + i > 128 ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") : DataResult.success(p_227638_);
		};
	}
	
	public CustomJigsawStructure(StructureSettings p_227627_, Holder<StructureTemplatePool> p_227628_, Optional<ResourceLocation> p_227629_, int p_227630_, HeightProvider p_227631_, boolean p_227632_, Optional<Heightmap.Types> p_227633_, int p_227634_) {
		super(p_227627_);
		this.startPool = p_227628_;
		this.startJigsawName = p_227629_;
		this.maxDepth = p_227630_;
		this.startHeight = p_227631_;
		this.useExpansionHack = p_227632_;
		this.projectStartToHeightmap = p_227633_;
		this.maxDistanceFromCenter = p_227634_;
	}
	
	public CustomJigsawStructure(StructureSettings p_227620_, Holder<StructureTemplatePool> p_227621_, int p_227622_, HeightProvider p_227623_, boolean p_227624_, Heightmap.Types p_227625_) {
		this(p_227620_, p_227621_, Optional.empty(), p_227622_, p_227623_, p_227624_, Optional.of(p_227625_), 80);
	}
	
	public CustomJigsawStructure(StructureSettings p_227614_, Holder<StructureTemplatePool> p_227615_, int p_227616_, HeightProvider p_227617_, boolean p_227618_) {
		this(p_227614_, p_227615_, Optional.empty(), p_227616_, p_227617_, p_227618_, Optional.empty(), 80);
	}
	
	@NotNull
	public Optional<GenerationStub> findGenerationPoint(GenerationContext p_227636_) {
		ChunkPos chunkpos = p_227636_.chunkPos();
		int i = this.startHeight.sample(p_227636_.random(), new WorldGenerationContext(p_227636_.chunkGenerator(), p_227636_.heightAccessor()));
		BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), i, chunkpos.getMinBlockZ());
		return CustomJigsawPlacement.addPieces(p_227636_, this.startPool, this.startJigsawName, this.maxDepth, blockpos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter);
	}
	
	@NotNull
	public StructureType<?> type() {
		return ModStructureTypes.CUSTOM_JIGSAW.get();
	}
}
