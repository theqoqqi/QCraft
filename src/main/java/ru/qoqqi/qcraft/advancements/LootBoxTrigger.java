package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import javax.annotation.Nonnull;

public class LootBoxTrigger extends SimpleCriterionTrigger<LootBoxTrigger.Instance> {

	@Override
	@NotNull
	public Instance createInstance(@Nonnull JsonObject json, @Nonnull Optional<ContextAwarePredicate> entityPredicate, @Nonnull DeserializationContext conditionsParser) {
		return new Instance(entityPredicate, ItemPredicate.fromJson(json.get("item")));
	}

	public void trigger(ServerPlayer player, ItemStack item) {
		this.trigger(player, (instance) -> {
			return instance.matches(item);
		});
	}

	public static class Instance extends AbstractCriterionTriggerInstance {

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private final Optional<ItemPredicate> item;

		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		public Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) {
			super(player);

			this.item = item;
		}

		public boolean matches(ItemStack item) {
			return this.item.isPresent() && this.item.get().matches(item);
		}

		@NotNull
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();

			item.ifPresent(item -> {
				jsonObject.add("item", item.serializeToJson());
			});

			return jsonObject;
		}
	}
}