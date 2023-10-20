package ru.qoqqi.qcraft.entities.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.entities.JellyBlob;
import ru.qoqqi.qcraft.entities.models.JellyBlobModel;
import ru.qoqqi.qcraft.entities.models.Layers;

@OnlyIn(Dist.CLIENT)
public class JellyBlobRenderer extends MobRenderer<JellyBlob, JellyBlobModel> {

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final ResourceLocation TINTABLE_TEXTURE_LOCATION = location("default");

	private static final Map<JellyBlob.JellyBlobType, ResourceLocation> textureLocationCache
			= new HashMap<>();

	@NotNull
	private static ResourceLocation location(String internalName) {
		String path = "textures/entity/jelly_blob/" + internalName + ".png";

		return new ResourceLocation(QCraft.MOD_ID, path);
	}

	public JellyBlobRenderer(EntityRendererProvider.Context context) {
		super(context, new JellyBlobModel(context.bakeLayer(Layers.JELLY_BLOB_LAYER)), 0.15f);
	}

	@Override
	protected void setupRotations(@NotNull JellyBlob jellyBlob, @NotNull PoseStack poseStack, float x, float y, float z) {
		super.setupRotations(jellyBlob, poseStack, x, y, z);

		if (jellyBlob.isFoodGained() && jellyBlob.deathTime > 0) {
			preventFlip(jellyBlob, poseStack, z);
		}
	}

	private void preventFlip(@NotNull JellyBlob jellyBlob, @NotNull PoseStack poseStack, float z) {
		var f = ((float) jellyBlob.deathTime + z - 1.0F) / 20.0F * 1.6F;

		f = Mth.sqrt(f);

		if (f > 1.0F) {
			f = 1.0F;
		}

		poseStack.mulPose(Axis.ZP.rotationDegrees(-f * this.getFlipDegrees(jellyBlob)));
	}

	@Nullable
	@Override
	protected RenderType getRenderType(@NotNull JellyBlob jellyBlob, boolean bodyVisible, boolean translucent, boolean glowing) {
		return super.getRenderType(jellyBlob, bodyVisible, true, glowing);
	}

	@NotNull
	public ResourceLocation getTextureLocation(@NotNull JellyBlob entity) {
		var blobType = entity.getBlobType();

		return getTextureLocation(blobType);
	}

	public static boolean shouldTintTexture(@NotNull JellyBlob.JellyBlobType blobType) {
		return getTextureLocation(blobType) == TINTABLE_TEXTURE_LOCATION;
	}

	@NotNull
	public static ResourceLocation getTextureLocation(@NotNull JellyBlob.JellyBlobType blobType) {
		return textureLocationCache.computeIfAbsent(blobType, JellyBlobRenderer::computeTextureLocation);
	}

	private static ResourceLocation computeTextureLocation(JellyBlob.JellyBlobType blobType) {
		var location = location(blobType.internalName);

		return isTextureExists(location) ? location : TINTABLE_TEXTURE_LOCATION;
	}

	private static boolean isTextureExists(ResourceLocation location) {
		return Minecraft.getInstance()
				.getResourceManager()
				.getResource(location)
				.isPresent();
	}
}
