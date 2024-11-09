package ru.qoqqi.qcraft.entities.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;

import java.util.function.Predicate;

public class CustomNonTameRandomTargetGoal<T extends LivingEntity> extends NonTameRandomTargetGoal<T> {

	public CustomNonTameRandomTargetGoal(
			TamableAnimal tamableMob,
			Class<T> targetType,
			boolean mustSee,
			Predicate<LivingEntity> targetPredicate
	) {
		super(tamableMob, targetType, mustSee, targetPredicate);
	}

	public Class<T> getTargetType() {
		return this.targetType;
	}
}
