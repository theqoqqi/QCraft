package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;

public class LootBoxTrigger extends AbstractCriterionTrigger<LootBoxTrigger.Instance> {
	private static final ResourceLocation ID = new ResourceLocation(QCraft.MOD_ID, "open_loot_box");
	
	@Nonnull
	public ResourceLocation getId() {
		return ID;
	}
	
	@Nonnull
	public LootBoxTrigger.Instance deserializeTrigger(@Nonnull JsonObject json, @Nonnull EntityPredicate.AndPredicate entityPredicate, @Nonnull ConditionArrayParser conditionsParser) {
		return new LootBoxTrigger.Instance(entityPredicate, ItemPredicate.deserialize(json.get("item")));
	}
	
	public void trigger(ServerPlayerEntity player, ItemStack item) {
		this.triggerListeners(player, (instance) -> {
			return instance.test(item);
		});
	}
	
	public static class Instance extends CriterionInstance {
		private final ItemPredicate item;
		
		public Instance(EntityPredicate.AndPredicate player, ItemPredicate item) {
			super(LootBoxTrigger.ID, player);
			this.item = item;
		}
		
		public boolean test(ItemStack item) {
			return this.item.test(item);
		}
		
		@Nonnull
		public JsonObject serialize(@Nonnull ConditionArraySerializer conditions) {
			JsonObject jsonobject = super.serialize(conditions);
			jsonobject.add("item", this.item.serialize());
			return jsonobject;
		}
	}
}