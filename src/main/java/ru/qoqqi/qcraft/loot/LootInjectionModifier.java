package ru.qoqqi.qcraft.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class LootInjectionModifier extends LootModifier {

	public static Codec<LootInjectionModifier> CODEC = RecordCodecBuilder.create(
			instance -> instance
					.group(
							LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
							ResourceLocation.CODEC.fieldOf("injection").forGetter(lm -> lm.table)
					)
					.apply(instance, LootInjectionModifier::new)
	);

	private final ResourceLocation table;

	protected LootInjectionModifier(LootItemCondition[] conditionsIn, ResourceLocation table) {
		super(conditionsIn);
		this.table = table;
	}

	@Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		var level = context.getLevel();
		var lootParams = new LootParams.Builder(level)
				.create(LootContextParamSets.EMPTY);
		var lootTable = level.getServer().getLootData().getLootTable(table);

		generatedLoot.addAll(lootTable.getRandomItems(lootParams));

		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}
