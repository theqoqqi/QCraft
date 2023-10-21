package ru.qoqqi.qcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ru.qoqqi.qcraft.blockentities.JourneyRewardBlockEntity;
import ru.qoqqi.qcraft.blockentities.ModBlockEntityTypes;
import ru.qoqqi.qcraft.boxes.LootBox;
import ru.qoqqi.qcraft.journey.JourneyStage;
import ru.qoqqi.qcraft.journey.JourneyStages;
import ru.qoqqi.qcraft.leveldata.JourneyLevelData;
import ru.qoqqi.qcraft.network.JourneyPlaceVisitedPacket;
import ru.qoqqi.qcraft.network.ModPacketHandler;

public class JourneyRewardBlock extends BaseEntityBlock {

	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

	private static final Map<JourneyStage, JourneyRewardBlock> byStages = new HashMap<>();

	private final JourneyStage stage;

	private final LootBox lootBox;

	public JourneyRewardBlock(Properties properties, JourneyStage stage, LootBox lootBox) {
		super(properties);
		this.stage = stage;
		this.lootBox = lootBox;

		byStages.put(stage, this);
	}

	@SuppressWarnings("deprecation")
	@Override
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}

		if (player.isCreative() && player.isSecondaryUseActive()) {
			JourneyStage nextStage = getNextStage();
			String message = String.format("Stage index changed to %s", nextStage.name);
			BlockState blockState = byStages.get(nextStage).defaultBlockState();

			player.sendSystemMessage(Component.literal(message));
			level.setBlock(pos, blockState, 2);

			return InteractionResult.SUCCESS;
		}

		JourneyLevelData levelData = JourneyLevelData.getInstance(serverLevel);
		UUID playerUuid = player.getUUID();

		if (levelData.isVisitedBy(stage, playerUuid)) {
			return InteractionResult.FAIL;
		}

		ItemStack itemStack = new ItemStack(asItem());
		lootBox.openSilently(player, itemStack, pos);
		JourneyPlaceVisitedPacket packet = new JourneyPlaceVisitedPacket(stage);

		levelData.addVisitor(stage, playerUuid);
		ModPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);

		return InteractionResult.SUCCESS;
	}

	private JourneyStage getNextStage() {
		JourneyStage nextStage = stage.next();

		if (nextStage == null) {
			nextStage = JourneyStages.getFirst();
		}

		return nextStage;
	}

	@SuppressWarnings("deprecation")
	public boolean useShapeForLightOcclusion(@Nonnull BlockState state) {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Nonnull
	public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter levelIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return SHAPE;
	}

	@NotNull
	public RenderShape getRenderShape(@NotNull BlockState pState) {
		return RenderShape.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new JourneyRewardBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return type == ModBlockEntityTypes.JOURNEY_REWARD.get()
				? (pLevel, pPos, pState, pBlockEntity) -> ((JourneyRewardBlockEntity) pBlockEntity).tick()
				: null;
	}
}
