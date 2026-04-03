package ru.qoqqi.qcraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SolvePuzzleTrigger extends SimpleCriterionTrigger<SolvePuzzleTrigger.Instance> {

	public void trigger(ServerPlayer player, BlockPos pos) {
		this.trigger(player, (instance) -> instance.matches(player.serverLevel(), pos));
	}

	@Override
	@NotNull
	public Codec<Instance> codec() {
		return Instance.CODEC;
	}

	public static class Instance implements SimpleCriterionTrigger.SimpleInstance {

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private final Optional<ContextAwarePredicate> player;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private final Optional<LocationPredicate> location;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public Instance(Optional<ContextAwarePredicate> player, Optional<LocationPredicate> location) {
			this.player = player;
			this.location = location;
		}

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create((p_308116_) ->
				p_308116_.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(Instance::player),
						ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(Instance::location)
				).apply(p_308116_, Instance::new)
		);

		public boolean matches(ServerLevel level, BlockPos pos) {
			if (location.isEmpty()) return false;

			var x = (double) pos.getX() + 0.5D;
			var y = (double) pos.getY() + 0.5D;
			var z = (double) pos.getZ() + 0.5D;

			return location.get().matches(level, x, y, z);
		}

		@NotNull
		@Override
		public Optional<ContextAwarePredicate> player() {
			return player;
		}

		@NotNull
		public Optional<LocationPredicate> location() {
			return location;
		}
	}
}