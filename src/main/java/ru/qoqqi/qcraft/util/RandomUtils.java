package ru.qoqqi.qcraft.util;

import net.minecraft.util.WeightedList;

public class RandomUtils {
	
	@SafeVarargs
	public static <T> WeightedList<T> createWeightedList(T... entries) {
		WeightedList<T> list = new WeightedList<>();
		
		for (T entry : entries) {
			list.addWeighted(entry, 1);
		}
		
		return list;
	}
	
	@SafeVarargs
	public static <T> WeightedList<T> createWeightedList(WeightedEntry<T>... entries) {
		WeightedList<T> list = new WeightedList<>();
		
		for (WeightedEntry<T> entry : entries) {
			list.addWeighted(entry.value, entry.weight);
		}
		
		return list;
	}
	
	public static class WeightedEntry<T> {
		
		final T value;
		final int weight;
		
		public WeightedEntry(int weight, T value) {
			this.value = value;
			this.weight = weight;
		}
	}
}
