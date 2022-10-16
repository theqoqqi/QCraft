package ru.qoqqi.qcraft.blockentities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.blockentities.ItemPedestal;

public class ItemPedestalBlockEntityRenderer implements BlockEntityRenderer<BlockEntity> {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	
	public ItemPedestalBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		// empty
	}
	
	@Override
	public void render(
			@Nonnull BlockEntity blockEntity,
			float partialTicks,
			@Nonnull PoseStack poseStack,
			@Nonnull MultiBufferSource buffer,
			int combinedLight,
			int combinedOverlay
	) {
		if (!(blockEntity instanceof ItemPedestal itemPedestal)) {
			return;
		}
		
		ItemStack itemStack = itemPedestal.getItemStack();
		
		if (itemStack.isEmpty()) {
			return;
		}
		
		float y = getItemStackY(itemPedestal, partialTicks);
		float rotation = getItemStackRotation(itemPedestal, partialTicks);
		TransformType transformType = TransformType.GROUND;
		
		poseStack.pushPose();
		
		poseStack.translate(0.5, 0.5 + y, 0.5);
		poseStack.mulPose(Vector3f.YP.rotation(rotation));
		poseStack.scale(1.25f, 1.25f, 1.25f);
		
		itemRenderer.renderStatic(itemStack, transformType, combinedLight, combinedOverlay, poseStack, buffer, 0);
		
		poseStack.popPose();
	}
	
	private float getItemStackY(@Nonnull ItemPedestal itemPedestal, float partialTicks) {
		ItemStack itemStack = itemPedestal.getItemStack();
		float y = Mth.sin((itemPedestal.getAge() + partialTicks) / 10.0F + itemPedestal.getHoverStart()) * 0.1F + 0.1F;
		
		Level level = itemPedestal.getLevel2();
		BakedModel model = this.itemRenderer.getModel(itemStack, level, null, 0);
		
		//noinspection deprecation
		y += model.getTransforms().getTransform(TransformType.GROUND).scale.y();
		
		return itemStack.getItem() instanceof BlockItem ? y : y + 0.125f;
	}
	
	private float getItemStackRotation(@Nonnull ItemPedestal itemPedestal, float partialTicks) {
		return ((float) itemPedestal.getAge() + partialTicks) / 20.0F + itemPedestal.getHoverStart();
	}
}
