package ru.qoqqi.qcraft.entities.models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import ru.qoqqi.qcraft.entities.JellyBlob;
import ru.qoqqi.qcraft.entities.renderers.JellyBlobRenderer;

@OnlyIn(Dist.CLIENT)
public class JellyBlobModel extends ListModel<JellyBlob> {

	private static final Logger LOGGER = LogManager.getLogger();

	protected final ModelPart body;
	protected final ModelPart eyes;
	protected final ModelPart mouth;

	protected final Vector3f bodyOffset;
	protected final Vector3f eyesOffset;
	protected final Vector3f mouthOffset;

	private float redMultiplier;
	private float greenMultiplier;
	private float blueMultiplier;
	private float alphaMultiplier;

	public JellyBlobModel(ModelPart root) {
		super();

		body = root.getChild("body");
		eyes = root.getChild("eyes");
		mouth = root.getChild("mouth");

		bodyOffset = new Vector3f(body.x, body.y, body.z);
		eyesOffset = new Vector3f(eyes.x, eyes.y, eyes.z);
		mouthOffset = new Vector3f(mouth.x, mouth.y, mouth.z);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();

		CubeListBuilder bodyCubes = CubeListBuilder.create()
				.texOffs(0, 0).addBox(-8.0F, -9.0F, -8.0F, 16.0F, 9.0F, 16.0F)
				.texOffs(0, 25).addBox(-7.0F, -15.0F, -7.0F, 14.0F, 6.0F, 14.0F)
				.texOffs(0, 45).addBox(-6.0F, -16.0F, -6.0F, 12.0F, 1.0F, 12.0F);

		CubeListBuilder eyeCubes = CubeListBuilder.create()
				.texOffs(8, 7).addBox(2.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F)
				.texOffs(0, 7).addBox(-5.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F);

		CubeListBuilder mouthCubes = CubeListBuilder.create()
				.texOffs(0, 4).addBox(-3.0F, 0.0F, -0.1F, 6.0F, 3.0F, 0.0F)
				.texOffs(4, 25).addBox(-4.0F, -1.0F, -0.2F, 1.0F, 5.0F, 1.0F)
				.texOffs(0, 25).addBox(3.0F, -1.0F, -0.2F, 1.0F, 5.0F, 1.0F)
				.texOffs(0, 2).addBox(-3.0F, -1.0F, -0.2F, 6.0F, 1.0F, 1.0F)
				.texOffs(0, 0).addBox(-3.0F, 3.0F, -0.2F, 6.0F, 1.0F, 1.0F);

		partDefinition.addOrReplaceChild("body", bodyCubes, PartPose.offset(0.0F, 24.0F, 0.0F));
		partDefinition.addOrReplaceChild("eyes", eyeCubes, PartPose.offset(0.0F, 13.0F, -7.0F));
		partDefinition.addOrReplaceChild("mouth", mouthCubes, PartPose.offset(0.0F, 18.0F, -8.0F));

		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@NotNull
	public Iterable<ModelPart> parts() {
		return ImmutableList.of(eyes, body, mouth);
	}

	public void prepareMobModel(@NotNull JellyBlob jellyBlob, float limbSwing, float limbSwingAmount, float partialTick) {
		super.prepareMobModel(jellyBlob, limbSwing, limbSwingAmount, partialTick);

		var blobType = jellyBlob.getBlobType();

		redMultiplier = 1.0f;
		greenMultiplier = 1.0f;
		blueMultiplier = 1.0f;
		alphaMultiplier = 0.9f;

		if (JellyBlobRenderer.shouldTintTexture(blobType)) {
			redMultiplier *= blobType.getRed(jellyBlob);
			greenMultiplier *= blobType.getGreen(jellyBlob);
			blueMultiplier *= blobType.getBlue(jellyBlob);
		}

		if (jellyBlob.deathTime > 0) {
			alphaMultiplier *= (float) Math.sqrt(1 - (jellyBlob.deathTime / 10f));
		}
	}

	@Override
	public void renderToBuffer(
			@NotNull PoseStack poseStack,
			@NotNull VertexConsumer vertexConsumer,
			int packedLight,
			int packedOverlay,
			float red,
			float green,
			float blue,
			float alpha
	) {
		super.renderToBuffer(
				poseStack,
				vertexConsumer,
				packedLight,
				packedOverlay,
				red * redMultiplier,
				green * greenMultiplier,
				blue * blueMultiplier,
				alpha * alphaMultiplier
		);
	}

	public void setupAnim(@NotNull JellyBlob entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		var inflationScale = Mth.cos(ageInTicks * 0.1F);
		var modelScale = JellyBlob.MODEL_SCALE;
		var localEyesScale = 1f;
		var localMouthScale = 1f;

		if (entity.isFoodGained()) {
			var blowUpModelScale = JellyBlob.BLOWING_UP_MODEL_SCALE;
			var blowUpLocalEyesScale = 0.5f;
			var blowUpLocalMouthScale = 0.5f;
			var blowUpProgress = entity.getBlowUpProgress();
			var easedProgress = easeProgress(blowUpProgress);
			var invertedProgress = 1 - easedProgress;

			inflationScale *= invertedProgress;
			modelScale = Mth.lerp(easedProgress, modelScale, blowUpModelScale);
			localEyesScale = Mth.lerp(easedProgress, localEyesScale, blowUpLocalEyesScale);
			localMouthScale = Mth.lerp(easedProgress, localMouthScale, blowUpLocalMouthScale);
		}

		var xzModelScale = modelScale + inflationScale * 0.05f;
		var yModelScale = modelScale - inflationScale * 0.1f;

		setupAnim(body, bodyOffset, xzModelScale, yModelScale);
		setupAnim(eyes, eyesOffset, xzModelScale, yModelScale);
		setupAnim(mouth, mouthOffset, xzModelScale, yModelScale);

		setupLocalScale(eyes, localEyesScale);
		setupLocalScale(mouth, localMouthScale);
	}

	private void setupAnim(ModelPart part, Vector3f offset, float xzScale, float yScale) {
		part.xScale = xzScale;
		part.yScale = yScale;
		part.zScale = xzScale;
		part.x = body.x + (offset.x - body.x) * xzScale;
		part.y = body.y + (offset.y - body.y) * yScale;
		part.z = body.z + (offset.z - body.z) * xzScale;
	}

	private void setupLocalScale(ModelPart part, float localScale) {
		part.xScale *= localScale;
		part.yScale *= localScale;
		part.zScale *= localScale;
	}

	private float easeProgress(float p) {
		return p < 0.5
				? 4 * p * p * p
				: (float) (1 - Math.pow(-2 * p + 2, 3) / 2);
	}
}
