package ru.qoqqi.qcraft;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class ModTags {

	public static class DamageTypes {

		public static final TagKey<DamageType> AVOIDS_THORNS = create(Registries.DAMAGE_TYPE, "avoids_thorns");
	}

	@SuppressWarnings("SameParameterValue")
	private static <T> TagKey<T> create(ResourceKey<Registry<T>> registry, String name) {
		return TagKey.create(registry, new ResourceLocation(QCraft.MOD_ID, name));
	}
}
