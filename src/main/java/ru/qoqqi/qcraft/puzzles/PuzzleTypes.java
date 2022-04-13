package ru.qoqqi.qcraft.puzzles;

public class PuzzleTypes {
	
	public static final PuzzleType PUZZLE_EASY = PuzzleType.builder()
			.withIngredientRange(2, 6)
			.withUniqueIngredientRange(2, 3)
			.withRecipeOutputStackRange(1, 64)
			.withSolutionSize(3)
			.build();
	
	public static final PuzzleType PUZZLE_NORMAL = PuzzleType.builder()
			.withIngredientRange(3, 9)
			.withUniqueIngredientRange(2, 9)
			.withRecipeOutputStackRange(1, 64)
			.withSolutionSize(3)
			.build();
	
	public static final PuzzleType PUZZLE_HARD = PuzzleType.builder()
			.withIngredientRange(3, 9)
			.withUniqueIngredientRange(2, 9)
			.withRecipeOutputStackRange(1, 64)
			.withSplitProbability(0.8f)
			.withSolutionSize(3)
			.build();
}
