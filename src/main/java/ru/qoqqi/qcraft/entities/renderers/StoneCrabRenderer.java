package ru.qoqqi.qcraft.entities.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.entities.StoneCrab;
import ru.qoqqi.qcraft.entities.models.Layers;
import ru.qoqqi.qcraft.entities.models.StoneCrabModel;

@OnlyIn(Dist.CLIENT)
public class StoneCrabRenderer extends MobRenderer<StoneCrab, StoneCrabModel> {

	private static final ResourceLocation STONE_CRAB_LOCATION =
			new ResourceLocation(QCraft.MOD_ID, "textures/entity/stone_crab.png");

	public StoneCrabRenderer(EntityRendererProvider.Context context) {
		super(context, new StoneCrabModel(context.bakeLayer(Layers.STONE_CRAB_LAYER)), 0.25f);
	}

	@NotNull
	public ResourceLocation getTextureLocation(@NotNull StoneCrab entity) {
		return STONE_CRAB_LOCATION;
	}
}
