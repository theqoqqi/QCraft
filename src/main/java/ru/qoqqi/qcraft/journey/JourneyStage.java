package ru.qoqqi.qcraft.journey;

import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public class JourneyStage {

	public final String name;

	public final Supplier<ItemStack> notesSupplier;

	public JourneyStage(String name, Supplier<ItemStack> notesSupplier) {
		this.name = name;
		this.notesSupplier = notesSupplier;
	}

	@Nullable
	public JourneyStage previous() {
		return JourneyStages.getPrevious(this);
	}

	@Nullable
	public JourneyStage next() {
		return JourneyStages.getNext(this);
	}

	public ItemStack createNotes() {
		return notesSupplier.get(); // TODO:
	}
}
