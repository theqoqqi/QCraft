package ru.qoqqi.qcraft.entities.models;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import ru.qoqqi.qcraft.entities.StoneCrab;

@OnlyIn(Dist.CLIENT)
public class StoneCrabModel extends AgeableListModel<StoneCrab> {
	
	protected final ModelPart body;
	protected final ModelPart eyes;
	
	protected final ModelPart leftJaw;
	protected final ModelPart rightJaw;
	
	protected final ModelPart leftFrontLeg;
	protected final ModelPart rightFrontLeg;
	protected final ModelPart leftMiddleLeg;
	protected final ModelPart rightMiddleLeg;
	protected final ModelPart leftHindLeg;
	protected final ModelPart rightHindLeg;
	
	public StoneCrabModel(ModelPart root) {
		super(false, 0f, 0f, 0f, 2f, 24f);
		
		eyes = root.getChild("eyes");
		body = root.getChild("body");
		
		leftJaw = root.getChild("left_jaw");
		rightJaw = root.getChild("right_jaw");
		
		leftFrontLeg = root.getChild("left_front_leg");
		rightFrontLeg = root.getChild("right_front_leg");
		leftMiddleLeg = root.getChild("left_middle_leg");
		rightMiddleLeg = root.getChild("right_middle_leg");
		leftHindLeg = root.getChild("left_hind_leg");
		rightHindLeg = root.getChild("right_hind_leg");
	}
	
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		
		var bodyCubes = CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -4.0F, -5.0F, 10.0F, 3.0F, 10.0F)
				.texOffs(0, 13).addBox(-4.0F, -5.0F, -4.0F, 8.0F, 1.0F, 8.0F);
		
		var eyeCubes = CubeListBuilder.create()
				.texOffs(0, 0).addBox(1.0F, -5.0F, -5.0F, 2.0F, 1.0F, 1.0F)
				.texOffs(0, 0).mirror().addBox(-3.0F, -5.0F, -5.0F, 2.0F, 1.0F, 1.0F);
		
		var leftJawCubes = CubeListBuilder.create()
				.texOffs(0, 2).addBox(0.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F);
		
		var rightJawCubes = CubeListBuilder.create()
				.texOffs(0, 2).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 1.0F, 1.0F);
		
		var smallLegCubes = CubeListBuilder.create()
				.texOffs(30, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 2.0F, 2.0F);
		
		var leftMiddleLegCubes = CubeListBuilder.create()
				.texOffs(30, 5).addBox(-1.0F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F);
		
		var rightMiddleLegCubes = CubeListBuilder.create()
				.texOffs(30, 5).addBox(-2.0F, 0.0F, -1.5F, 3.0F, 2.0F, 3.0F);
		
		partDefinition.addOrReplaceChild("body", bodyCubes, PartPose.offset(0.0F, 24.0F, 0.0F));
		partDefinition.addOrReplaceChild("eyes", eyeCubes, PartPose.offset(0.0F, 24.0F, 0.0F));
		
		partDefinition.addOrReplaceChild("left_jaw", leftJawCubes, PartPose.offset(1.0F, 22.0F, -5.0F));
		partDefinition.addOrReplaceChild("right_jaw", rightJawCubes, PartPose.offset(-1.0F, 22.0F, -5.0F));
		
		partDefinition.addOrReplaceChild("left_front_leg", smallLegCubes, PartPose.offset(5.0F, 22.0F, -5.0F));
		partDefinition.addOrReplaceChild("left_middle_leg", leftMiddleLegCubes, PartPose.offset(5.0F, 22.0F, 0.0F));
		partDefinition.addOrReplaceChild("left_hind_leg", smallLegCubes, PartPose.offset(5.0F, 22.0F, 5.0F));
		partDefinition.addOrReplaceChild("right_front_leg", smallLegCubes, PartPose.offset(-5.0F, 22.0F, -5.0F));
		partDefinition.addOrReplaceChild("right_middle_leg", rightMiddleLegCubes, PartPose.offset(-5.0F, 22.0F, 0.0F));
		partDefinition.addOrReplaceChild("right_hind_leg", smallLegCubes, PartPose.offset(-5.0F, 22.0F, 5.0F));
		
		return LayerDefinition.create(meshDefinition, 64, 32);
	}
	
	@NotNull
	protected Iterable<ModelPart> headParts() {
		return ImmutableList.of();
	}
	
	@NotNull
	protected Iterable<ModelPart> bodyParts() {
		return ImmutableList.of(
				eyes,
				body,
				leftJaw,
				rightJaw,
				leftFrontLeg,
				rightFrontLeg,
				leftMiddleLeg,
				rightMiddleLeg,
				leftHindLeg,
				rightHindLeg
		);
	}
	
	public void setupAnim(@NotNull StoneCrab entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		var pi = (float) Math.PI;
		
		boolean isAnimatingJaws = entity.updateJawsAnimationState(ageInTicks);
		
		if (isAnimatingJaws) {
			var rotationProgress = entity.getJawsRotationProgress(ageInTicks);
			var halfPi = pi / 2;
			
			// left zRot + 90, right zRot - 90
			leftJaw.zRot = halfPi * rotationProgress;
			rightJaw.zRot = -halfPi * rotationProgress;
			
		} else {
			leftJaw.zRot = 0;
			rightJaw.zRot = 0;
		}
		
		rightFrontLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, pi);
		leftFrontLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, 0);
		rightMiddleLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, pi / 2);
		leftMiddleLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, pi / 2);
		rightHindLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, 0);
		leftHindLeg.xRot = getLimbSwingRotation(limbSwing, limbSwingAmount, pi);
	}
	
	private float getLimbSwingRotation(float limbSwing, float limbSwingAmount, float scaledOffset) {
		var limbSpeed = 1.5f;
		var limbPower = 6f;
		var scaledLimbSwing = limbSwing * limbSpeed;
		
		return Mth.cos(scaledLimbSwing + scaledOffset) * limbPower * limbSwingAmount;
	}
}
