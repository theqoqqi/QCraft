package ru.qoqqi.qcraft.util;

import java.util.Random;
import java.util.function.IntPredicate;

public class IntRange implements IntPredicate {
	
	public final int min;
	
	public final int max;
	
	private IntRange(int min, int max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public boolean test(int value) {
		return value >= min && value <= max;
	}
	
	public int getRandomValue(Random random) {
		return random.nextInt(max - min + 1) + min;
	}
	
	public static IntRange of(int min, int max) {
		return new IntRange(min, max);
	}
	
	public static IntRange ofMin(int min) {
		return new IntRange(min, Integer.MAX_VALUE);
	}
	
	public static IntRange ofMax(int max) {
		return new IntRange(Integer.MIN_VALUE, max);
	}
}
