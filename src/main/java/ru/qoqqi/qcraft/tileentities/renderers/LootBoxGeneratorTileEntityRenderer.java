package ru.qoqqi.qcraft.tileentities.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.tileentities.LootBoxGeneratorTileEntity;

public class LootBoxGeneratorTileEntityRenderer extends TileEntityRenderer<LootBoxGeneratorTileEntity> {
	
	private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
	
	public LootBoxGeneratorTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}
	
	@Override
	public void render(
			@Nonnull LootBoxGeneratorTileEntity tileEntity,
			float partialTicks,
			@Nonnull MatrixStack matrixStack,
			@Nonnull IRenderTypeBuffer buffer,
			int combinedLight,
			int combinedOverlay
	) {
		ItemStack itemStack = tileEntity.getItemStack();
		
		if (itemStack.isEmpty()) {
			return;
		}
		
		float y = getItemStackY(tileEntity, partialTicks);
		float rotation = getItemStackRotation(tileEntity, partialTicks);
		TransformType transformType = TransformType.GROUND;
		
		matrixStack.push();
		
		matrixStack.translate(0.5, 0.5 + y, 0.5);
		matrixStack.rotate(Vector3f.YP.rotation(rotation));
		matrixStack.scale(1.25f, 1.25f, 1.25f);
		
		itemRenderer.renderItem(itemStack, transformType, combinedLight, combinedOverlay, matrixStack, buffer);
		
		matrixStack.pop();
	}
	
	private float getItemStackY(@Nonnull LootBoxGeneratorTileEntity tileEntity, float partialTicks) {
		ItemStack itemStack = tileEntity.getItemStack();
		float y = MathHelper.sin((tileEntity.getAge() + partialTicks) / 10.0F + tileEntity.hoverStart) * 0.1F + 0.1F;
		
		World world = tileEntity.getWorld();
		IBakedModel model = this.itemRenderer.getItemModelWithOverrides(itemStack, world, null);
		
		//noinspection deprecation
		y += model.getItemCameraTransforms().getTransform(TransformType.GROUND).scale.getY();
		
		return itemStack.getItem() instanceof BlockItem ? y : y + 0.125f;
	}
	
	private float getItemStackRotation(@Nonnull LootBoxGeneratorTileEntity tileEntity, float partialTicks) {
		return ((float) tileEntity.getAge() + partialTicks) / 20.0F + tileEntity.hoverStart;
	}
}
