package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import javax.annotation.Nonnull;

public class SolvePuzzleTrigger extends SimpleCriterionTrigger<SolvePuzzleTrigger.Instance> {

	@Override
	@Nonnull
	public Instance createInstance(@Nonnull JsonObject json, @Nonnull Optional<ContextAwarePredicate> entityPredicate, @Nonnull DeserializationContext conditionsParser) {
		var locationPredicate = LocationPredicate.fromJson(json.get("location"));
		return new Instance(entityPredicate, locationPredicate);
	}

	public void trigger(ServerPlayer player, BlockPos pos) {
		this.trigger(player, (instance) -> {
			return instance.matches(player.serverLevel(), pos);
		});
	}

	public static class Instance extends AbstractCriterionTriggerInstance {

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private final Optional<LocationPredicate> location;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public Instance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> location) {
			super(player);

			this.location = location;
		}

		public boolean matches(ServerLevel level, BlockPos pos) {
			if (location.isEmpty()) return false;

			var x = (double) pos.getX() + 0.5D;
			var y = (double) pos.getY() + 0.5D;
			var z = (double) pos.getZ() + 0.5D;

			return location.get().matches(level, x, y, z);
		}

		@Nonnull
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();

			location.ifPresent(location -> {
				jsonObject.add("location", location.serializeToJson());
			});

			return jsonObject;
		}
	}
}