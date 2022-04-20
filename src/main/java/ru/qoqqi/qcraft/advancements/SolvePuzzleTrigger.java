package ru.qoqqi.qcraft.advancements;

import com.google.gson.JsonObject;

import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;

import ru.qoqqi.qcraft.QCraft;

public class SolvePuzzleTrigger extends AbstractCriterionTrigger<SolvePuzzleTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation(QCraft.MOD_ID, "solve_puzzle");

   @Nonnull
   public ResourceLocation getId() {
      return ID;
   }

   @Nonnull
   public SolvePuzzleTrigger.Instance deserializeTrigger(@Nonnull JsonObject json, @Nonnull EntityPredicate.AndPredicate entityPredicate, @Nonnull ConditionArrayParser conditionsParser) {
      LocationPredicate locationPredicate = LocationPredicate.deserialize(json.get("location"));
      return new SolvePuzzleTrigger.Instance(entityPredicate, locationPredicate);
   }

   public void trigger(ServerPlayerEntity player, BlockPos pos) {
      this.triggerListeners(player, (instance) -> {
         return instance.test(player.getServerWorld(), pos);
      });
   }

   public static class Instance extends CriterionInstance {
      private final LocationPredicate location;

      public Instance(EntityPredicate.AndPredicate player, LocationPredicate location) {
         super(SolvePuzzleTrigger.ID, player);
         this.location = location;
      }

      public boolean test(ServerWorld world, BlockPos pos) {
         return this.location.test(world, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
      }

      @Nonnull
      public JsonObject serialize(@Nonnull ConditionArraySerializer conditions) {
         JsonObject jsonObject = super.serialize(conditions);
         jsonObject.add("location", this.location.serialize());
         return jsonObject;
      }
   }
}