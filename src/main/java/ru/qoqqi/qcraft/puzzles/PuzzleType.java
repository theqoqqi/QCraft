package ru.qoqqi.qcraft.puzzles;

import ru.qoqqi.qcraft.util.IntRange;

public class PuzzleType {
	
	protected final IntRange ingredientRange;
	
	protected final IntRange uniqueIngredientRange;
	
	protected final IntRange recipeOutputStackRange;
	
	protected final float splitProbability;
	
	public PuzzleType(IntRange ingredientRange, IntRange uniqueIngredientRange, IntRange recipeOutputStackRange, float splitProbability) {
		this.ingredientRange = ingredientRange;
		this.uniqueIngredientRange = uniqueIngredientRange;
		this.recipeOutputStackRange = recipeOutputStackRange;
		this.splitProbability = splitProbability;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		
		protected IntRange ingredientRange = IntRange.of(1, 9);
		
		protected IntRange uniqueIngredientRange = IntRange.of(1, 9);
		
		protected IntRange recipeOutputStackRange = IntRange.of(1, 64);
		
		protected float splitProbability = 0.0f;
		
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
		
		public PuzzleType build() {
			return new PuzzleType(ingredientRange, uniqueIngredientRange, recipeOutputStackRange, splitProbability);
		}
	}
}
