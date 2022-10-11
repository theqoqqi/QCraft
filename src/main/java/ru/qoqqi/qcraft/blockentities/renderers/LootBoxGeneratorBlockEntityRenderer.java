package ru.qoqqi.qcraft.blockentities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;
import com.mojang.math.Vector3f;
import net.minecraft.world.level.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blockentities.LootBoxGeneratorBlockEntity;

public class LootBoxGeneratorBlockEntityRenderer implements BlockEntityRenderer<LootBoxGeneratorBlockEntity> {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	
	public LootBoxGeneratorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		// empty
	}
	
	@Override
	public void render(
			@Nonnull LootBoxGeneratorBlockEntity blockEntity,
			float partialTicks,
			@Nonnull PoseStack poseStack,
			@Nonnull MultiBufferSource buffer,
			int combinedLight,
			int combinedOverlay
	) {
		ItemStack itemStack = blockEntity.getItemStack();
		
		if (itemStack.isEmpty()) {
			return;
		}
		
		float y = getItemStackY(blockEntity, partialTicks);
		float rotation = getItemStackRotation(blockEntity, partialTicks);
		TransformType transformType = TransformType.GROUND;
		
		poseStack.pushPose();
		
		poseStack.translate(0.5, 0.5 + y, 0.5);
		poseStack.mulPose(Vector3f.YP.rotation(rotation));
		poseStack.scale(1.25f, 1.25f, 1.25f);
		
		itemRenderer.renderStatic(itemStack, transformType, combinedLight, combinedOverlay, poseStack, buffer, 0);
		
		poseStack.popPose();
	}
	
	private float getItemStackY(@Nonnull LootBoxGeneratorBlockEntity blockEntity, float partialTicks) {
		ItemStack itemStack = blockEntity.getItemStack();
		float y = Mth.sin((blockEntity.getAge() + partialTicks) / 10.0F + blockEntity.hoverStart) * 0.1F + 0.1F;
		
		Level level = blockEntity.getLevel();
		BakedModel model = this.itemRenderer.getModel(itemStack, level, null, 0);
		
		//noinspection deprecation
		y += model.getTransforms().getTransform(TransformType.GROUND).scale.y();
		
		return itemStack.getItem() instanceof BlockItem ? y : y + 0.125f;
	}
	
	private float getItemStackRotation(@Nonnull LootBoxGeneratorBlockEntity blockEntity, float partialTicks) {
		return ((float) blockEntity.getAge() + partialTicks) / 20.0F + blockEntity.hoverStart;
	}
}
