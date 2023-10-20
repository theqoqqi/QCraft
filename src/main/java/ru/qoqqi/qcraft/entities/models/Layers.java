package ru.qoqqi.qcraft.entities.models;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import ru.qoqqi.qcraft.QCraft;

@Mod.EventBusSubscriber(modid = QCraft.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Layers {

	public static ModelLayerLocation STONE_CRAB_LAYER = create("stone_crab");

	public static ModelLayerLocation FIELD_MOUSE_LAYER = create("field_mouse");

	public static ModelLayerLocation JELLY_BLOB_LAYER = create("jelly_blob");

	@SubscribeEvent
	public static void registerLayerDefinition(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(STONE_CRAB_LAYER, StoneCrabModel::createBodyLayer);
		event.registerLayerDefinition(FIELD_MOUSE_LAYER, FieldMouseModel::createBodyLayer);
		event.registerLayerDefinition(JELLY_BLOB_LAYER, JellyBlobModel::createBodyLayer);
	}

	@SuppressWarnings("SameParameterValue")
	private static ModelLayerLocation create(String path) {
		return create(path, "main");
	}

	@SuppressWarnings("SameParameterValue")
	private static ModelLayerLocation create(String path, String model) {
		return new ModelLayerLocation(new ResourceLocation(QCraft.MOD_ID, path), model);
	}
}
