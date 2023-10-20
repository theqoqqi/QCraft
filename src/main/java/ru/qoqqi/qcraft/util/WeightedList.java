package ru.qoqqi.qcraft.util;

import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;

import java.util.Arrays;
import java.util.List;

public class WeightedList<T> {
	
	private final SimpleWeightedRandomList<T> list;
	
	private WeightedList(List<WeightedEntry<T>> entries, boolean ignoredWrapped) {
		SimpleWeightedRandomList.Builder<T> builder = SimpleWeightedRandomList.builder();
		
		entries.forEach(entry -> builder.add(entry.value, entry.weight));
		
		this.list = builder.build();
	}
	
	private WeightedList(List<? extends T> entries) {
		SimpleWeightedRandomList.Builder<T> builder = SimpleWeightedRandomList.builder();
		
		entries.forEach(entry -> builder.add(entry, 1));
		
		this.list = builder.build();
	}
	
	public T getRandomValue(RandomSource randomSource) {
		return list.getRandomValue(randomSource).orElse(null);
	}
	
	@SafeVarargs
	public static <T> WeightedList<T> create(WeightedEntry<T>... entries) {
		return new WeightedList<>(Arrays.asList(entries), true);
	}
	
	public static <T> WeightedList<T> create(List<? extends T> entries) {
		return new WeightedList<>(entries);
	}
	
	@SafeVarargs
	public static <T> WeightedList<T> create(T... entries) {
		return new WeightedList<>(Arrays.asList(entries));
	}
	
	public static class WeightedEntry<T> {
		
		final T value;
		final int weight;
		
		public WeightedEntry(T value) {
			this(1, value);
		}
		
		public WeightedEntry(int weight, T value) {
			this.value = value;
			this.weight = weight;
		}
	}
}
