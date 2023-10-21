package ru.qoqqi.qcraft.puzzles;

import ru.qoqqi.qcraft.util.IntRange;

public class PuzzleType {

	public final IntRange ingredientRange;

	public final IntRange uniqueIngredientRange;

	public final IntRange recipeOutputStackRange;

	public final float splitProbability;

	public final int solutionSize;

	public PuzzleType(IntRange ingredientRange, IntRange uniqueIngredientRange, IntRange recipeOutputStackRange, float splitProbability, int solutionSize) {
		this.ingredientRange = ingredientRange;
		this.uniqueIngredientRange = uniqueIngredientRange;
		this.recipeOutputStackRange = recipeOutputStackRange;
		this.splitProbability = splitProbability;
		this.solutionSize = solutionSize;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private IntRange ingredientRange = IntRange.of(1, 9);

		private IntRange uniqueIngredientRange = IntRange.of(1, 9);

		private IntRange recipeOutputStackRange = IntRange.of(1, 64);

		private float splitProbability = 0.0f;

		private int solutionSize = 3;

		private Builder() {
		}

		public Builder withIngredientRange(int min, int max) {
			this.ingredientRange = IntRange.of(min, max);
			return this;
		}

		public Builder withUniqueIngredientRange(int min, int max) {
			this.uniqueIngredientRange = IntRange.of(min, max);
			return this;
		}

		public Builder withRecipeOutputStackRange(int min, int max) {
			this.recipeOutputStackRange = IntRange.of(min, max);
			return this;
		}

		public Builder withSplitProbability(float splitProbability) {
			this.splitProbability = splitProbability;
			return this;
		}

		public Builder withSolutionSize(int solutionSize) {
			this.solutionSize = solutionSize;
			return this;
		}

		public PuzzleType build() {
			return new PuzzleType(ingredientRange, uniqueIngredientRange, recipeOutputStackRange, splitProbability, solutionSize);
		}
	}
}
