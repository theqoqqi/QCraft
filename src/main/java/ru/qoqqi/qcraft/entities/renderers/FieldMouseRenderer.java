package ru.qoqqi.qcraft.entities.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import ru.qoqqi.qcraft.QCraft;
import ru.qoqqi.qcraft.entities.FieldMouse;
import ru.qoqqi.qcraft.entities.models.FieldMouseModel;
import ru.qoqqi.qcraft.entities.models.Layers;

@OnlyIn(Dist.CLIENT)
public class FieldMouseRenderer extends MobRenderer<FieldMouse, FieldMouseModel> {
	private static final Map<Integer, ResourceLocation> FIELD_MOUSE_LOCATIONS = Map.of(
			FieldMouse.TYPE_PLAINS, location("textures/entity/field_mouse/plains.png"),
			FieldMouse.TYPE_FOREST, location("textures/entity/field_mouse/forest.png"),
			FieldMouse.TYPE_SAVANNA, location("textures/entity/field_mouse/savanna.png"),
			FieldMouse.TYPE_DESERT, location("textures/entity/field_mouse/desert.png"),
			FieldMouse.TYPE_SWAMP, location("textures/entity/field_mouse/swamp.png"),
			FieldMouse.TYPE_SNOWY, location("textures/entity/field_mouse/snowy.png")
	);
	
	@NotNull
	private static ResourceLocation location(String fileName) {
		return new ResourceLocation(QCraft.MOD_ID, fileName);
	}
	
	public FieldMouseRenderer(EntityRendererProvider.Context context) {
		super(context, new FieldMouseModel(context.bakeLayer(Layers.FIELD_MOUSE_LAYER)), 0.15f);
	}

	@NotNull
	public ResourceLocation getTextureLocation(@NotNull FieldMouse entity) {
		var mouseType = entity.getMouseType();
		
		if (!FIELD_MOUSE_LOCATIONS.containsKey(mouseType)) {
			return FIELD_MOUSE_LOCATIONS.get(FieldMouse.TYPE_PLAINS);
		}
		
		return FIELD_MOUSE_LOCATIONS.get(mouseType);
	}
}
