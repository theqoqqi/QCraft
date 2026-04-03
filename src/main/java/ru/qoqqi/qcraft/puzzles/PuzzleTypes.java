package ru.qoqqi.qcraft.puzzles;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PuzzleTypes {

	private static final Map<String, PuzzleType> byNames = new HashMap<>();

	public static final PuzzleType PUZZLE_EASY = register("puzzle_easy", builder -> builder
			.withIngredientRange(2, 6)
			.withUniqueIngredientRange(2, 3)
			.withRecipeOutputStackRange(1, 64)
			.withSolutionSize(3)
	);

	public static final PuzzleType PUZZLE_NORMAL = register("puzzle_normal", builder -> builder
			.withIngredientRange(3, 9)
			.withUniqueIngredientRange(2, 9)
			.withRecipeOutputStackRange(1, 64)
			.withSolutionSize(3)
	);

	public static final PuzzleType PUZZLE_HARD = register("puzzle_hard", builder -> builder
			.withIngredientRange(3, 9)
			.withUniqueIngredientRange(2, 9)
			.withRecipeOutputStackRange(1, 64)
			.withSplitProbability(0.8f)
			.withSolutionSize(3)
	);

	private static PuzzleType register(String name, Function<PuzzleType.Builder, PuzzleType.Builder> buildFunc) {
		var puzzleType = buildFunc.apply(PuzzleType.builder()).build(name);

		byNames.put(name, puzzleType);

		return puzzleType;
	}

	public static PuzzleType byName(String name) {
		return byNames.get(name);
	}
}
