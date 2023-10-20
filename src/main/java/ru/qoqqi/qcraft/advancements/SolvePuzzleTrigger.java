package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;

public class SolvePuzzleTrigger extends SimpleCriterionTrigger<SolvePuzzleTrigger.Instance> {
	
	private static final ResourceLocation ID = new ResourceLocation(QCraft.MOD_ID, "solve_puzzle");
	
	@Nonnull
	public ResourceLocation getId() {
		return ID;
	}
	
	@Override
	@Nonnull
	public Instance createInstance(@Nonnull JsonObject json, @Nonnull EntityPredicate.Composite entityPredicate, @Nonnull DeserializationContext conditionsParser) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(json.get("location"));
		return new Instance(entityPredicate, locationPredicate);
	}
	
	public void trigger(ServerPlayer player, BlockPos pos) {
		this.trigger(player, (instance) -> {
			return instance.matches(player.getLevel(), pos);
		});
	}
	
	public static class Instance extends AbstractCriterionTriggerInstance {
		
		private final LocationPredicate location;
		
		public Instance(EntityPredicate.Composite player, LocationPredicate location) {
			super(SolvePuzzleTrigger.ID, player);
			this.location = location;
		}
		
		public boolean matches(ServerLevel level, BlockPos pos) {
			return this.location.matches(level, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
		}
		
		@Nonnull
		public JsonObject serializeToJson(@Nonnull SerializationContext conditions) {
			JsonObject jsonObject = super.serializeToJson(conditions);
			jsonObject.add("location", this.location.serializeToJson());
			return jsonObject;
		}
	}
}