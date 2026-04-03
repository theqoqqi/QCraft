package ru.qoqqi.qcraft.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LootBoxTrigger extends SimpleCriterionTrigger<LootBoxTrigger.Instance> {

	public void trigger(ServerPlayer player, ItemStack item) {
		this.trigger(player, (instance) -> {
			return instance.matches(item);
		});
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
		private final Optional<ItemPredicate> item;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) {
			this.player = player;
			this.item = item;
		}

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create((p_308116_) -> {
			return p_308116_.group(
					ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player")
							.forGetter(Instance::player),
					ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item")
							.forGetter(Instance::item)
			).apply(p_308116_, Instance::new);
		});

		public boolean matches(ItemStack item) {
			return this.item.isPresent() && this.item.get().matches(item);
		}

		@Override
		@NotNull
		public Optional<ContextAwarePredicate> player() {
			return player;
		}

		@NotNull
		public Optional<ItemPredicate> item() {
			return item;
		}
	}
}