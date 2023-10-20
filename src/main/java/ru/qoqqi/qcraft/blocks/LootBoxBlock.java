package ru.qoqqi.qcraft.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.advancements.ModCriteriaTriggers;
import ru.qoqqi.qcraft.boxes.LootBox;

public class LootBoxBlock extends Block {

	private final LootBox lootBox;

	public LootBoxBlock(Properties properties, LootBox lootBox) {
		super(properties);
		this.lootBox = lootBox;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Nonnull
	public InteractionResult use(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			ItemStack itemStack = new ItemStack(asItem());

			level.destroyBlock(pos, false);

			InteractionResult result = this.lootBox.openWithActionResult(player, itemStack, pos);

			ModCriteriaTriggers.OPEN_PANDORAS_BOX.trigger((ServerPlayer) player);

			return result;
		}
	}
}
