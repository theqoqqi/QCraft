package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;

public class LootBoxTrigger extends SimpleCriterionTrigger<LootBoxTrigger.Instance> {
	
	private static final ResourceLocation ID = new ResourceLocation(QCraft.MOD_ID, "open_loot_box");
	
	@Nonnull
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	@NotNull
	protected Instance createInstance(JsonObject json, @NotNull ContextAwarePredicate entityPredicate, @NotNull DeserializationContext conditionsParser) {
		return new Instance(entityPredicate, ItemPredicate.fromJson(json.get("item")));
	}
	
	public void trigger(ServerPlayer player, ItemStack item) {
		this.trigger(player, (instance) -> {
			return instance.matches(item);
		});
	}
	
	public static class Instance extends AbstractCriterionTriggerInstance {
		
		private final ItemPredicate item;
		
		public Instance(ContextAwarePredicate player, ItemPredicate item) {
			super(LootBoxTrigger.ID, player);
			this.item = item;
		}
		
		public boolean matches(ItemStack item) {
			return this.item.matches(item);
		}
		
		@NotNull
		public JsonObject serializeToJson(@NotNull SerializationContext conditions) {
			JsonObject jsonobject = super.serializeToJson(conditions);
			jsonobject.add("item", this.item.serializeToJson());
			return jsonobject;
		}
	}
}