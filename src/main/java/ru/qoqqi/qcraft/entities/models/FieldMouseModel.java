package ru.qoqqi.qcraft.entities.models;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

import ru.qoqqi.qcraft.entities.FieldMouse;

public class FieldMouseModel extends QuadrupedModel<FieldMouse> {
	
	private final ModelPart tail;
	
	public FieldMouseModel(ModelPart root) {
		super(root, false, 0f, 0f, 1f, 1f, 24);
		
		tail = root.getChild("tail");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		
		var headCubes = CubeListBuilder.create()
				.texOffs(0, 12).addBox(-1.5F, -1.0F, -6.0F, 3.0F, 3.0F, 5.0F)
				.texOffs(0, 4).addBox(-0.5F, 0.0F, -6.5F, 1.0F, 1.0F, 1.0F)
				.texOffs(0, 6).addBox(1.5F, 1.0F, -6.0F, 3.0F, 1.0F, 0.0F)
				.texOffs(0, 7).addBox(1.5F, -1.0F, -6.0F, 2.0F, 1.0F, 0.0F)
				.texOffs(0, 6).addBox(-4.5F, 1.0F, -6.0F, 3.0F, 1.0F, 0.0F)
				.texOffs(0, 7).addBox(-3.5F, -1.0F, -6.0F, 2.0F, 1.0F, 0.0F)
				.texOffs(0, 0).addBox(0.5F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F)
				.texOffs(0, 0).mirror().addBox(-3.5F, -3.0F, -3.0F, 3.0F, 3.0F, 1.0F);
		
		var bodyCubes = CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -5.0F, -3.0F, 6.0F, 4.0F, 8.0F);
		
		var legCubes = CubeListBuilder.create()
				.texOffs(20, 0).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 1.0F, 2.0F);
		
		var tailCubes = CubeListBuilder.create()
				.texOffs(10, 14).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 6.0F);
		
		partDefinition.addOrReplaceChild("head", headCubes, PartPose.offset(0.0F, 21.0F, -2.0F));
		partDefinition.addOrReplaceChild("body", bodyCubes, PartPose.offset(0.0F, 24.0F, 0.0F));
		partDefinition.addOrReplaceChild("left_front_leg", legCubes, PartPose.offset(3.0F, 23.0F, -2.0F));
		partDefinition.addOrReplaceChild("right_front_leg", legCubes, PartPose.offset(-3.0F, 23.0F, -2.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", legCubes, PartPose.offset(3.0F, 23.0F, 5.0F));
		partDefinition.addOrReplaceChild("right_hind_leg", legCubes, PartPose.offset(-3.0F, 23.0F, 5.0F));
		partDefinition.addOrReplaceChild("tail", tailCubes, PartPose.offset(0.0F, 21.5F, 5.0F));
		
		return LayerDefinition.create(meshDefinition, 32, 32);
	}
	
	public void renderToBuffer(
			@NotNull PoseStack poseStack,
			@NotNull VertexConsumer buffer,
			int packedLight,
			int packedOverlay,
			float red,
			float green,
			float blue,
			float alpha
	) {
		if (young) {
			renderPartsToBuffer(headParts(), 0.25f, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			renderPartsToBuffer(bodyParts(), 0.25f, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		} else {
			renderPartsToBuffer(headParts(), 0.4f, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
			renderPartsToBuffer(bodyParts(), 0.4f, poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}
	
	private void renderPartsToBuffer(
			@NotNull Iterable<ModelPart> parts,
			float scale,
			@NotNull PoseStack poseStack,
			@NotNull VertexConsumer buffer,
			int packedLight,
			int packedOverlay,
			float red,
			float green,
			float blue,
			float alpha
	) {
		var yOffset = getYOffsetForScale(scale);
		
		poseStack.pushPose();
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0.0D, yOffset, 0.0D);
		
		parts.forEach((modelPart) -> {
			modelPart.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		});
		
		poseStack.popPose();
	}
	
	@NotNull
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(body, rightHindLeg, leftHindLeg, rightFrontLeg, leftFrontLeg, tail);
	}
	
	@Override
	public void setupAnim(@NotNull FieldMouse entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		
		tail.xRot = 22.5f;
		tail.yRot = Mth.PI + Mth.cos(ageInTicks * 0.6662F) * 0.9F;
	}
	
	/**
	 * Когда отрисовываем модель с определенным скейлом, ее нужно сдвигать по оси Y на определенное
	 * расстояние, чтобы она оставалась на своем месте. Почему в майне это так - хз.
	 * */
	private static float getYOffsetForScale(float scale) {
		return 1.5f * ((1f / scale) - 1f);
	}
}
