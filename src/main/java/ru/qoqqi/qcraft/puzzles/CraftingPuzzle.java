package ru.qoqqi.qcraft.puzzles;

import com.google.common.base.Equivalence;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class CraftingPuzzle {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final List<ResourceLocation> BLACKLIST = Arrays.asList(
			new ResourceLocation("minecraft:map"),
			new ResourceLocation("minecraft:filled_map")
	);
	
	private static final Map<PuzzleType, List<CraftingRecipe>> RECIPES_CACHE = new HashMap<>();
	
	private static final Equivalence<ItemStack> ITEM_STACK_EQUIVALENCE = new Equivalence<>() {
		@Override
		protected boolean doEquivalent(@Nonnull ItemStack a, @Nonnull ItemStack b) {
			return a.equals(b, true);
		}
		
		@Override
		protected int doHash(@Nonnull ItemStack item) {
			return item.getItem().hashCode() + item.getCount();
		}
	};
	
	private final List<ItemStack> ingredients;
	
	private final Set<Equivalence.Wrapper<ItemStack>> rightSolution;
	
	public CraftingPuzzle(List<ItemStack> ingredients, List<ItemStack> rightSolution) {
		this.ingredients = ingredients;
		this.rightSolution = listToSet(rightSolution);
	}
	
	public boolean isCorrectSolution(List<ItemStack> playerSolution) {
		return isCorrectSolution(listToSet(playerSolution));
	}
	
	private boolean isCorrectSolution(Set<Equivalence.Wrapper<ItemStack>> playerSolution) {
		return rightSolution.equals(playerSolution);
	}
	
	public List<ItemStack> getIngredients() {
		return ingredients;
	}
	
	public static CraftingPuzzle generate(Level level, Random random, PuzzleType config) {
		List<CraftingRecipe> recipes = getRandomRecipes(level, random, config);
		
		List<ItemStack> ingredients = new ArrayList<>();
		List<ItemStack> solution = new ArrayList<>();
		
		recipes.forEach(recipe -> {
			recipe.getIngredients().stream()
					.filter(CraftingPuzzle::isNonDummyIngredient)
					.forEach(ingredient -> {
						ingredients.add(getRandomIngredient(ingredient, random));
					});
			
			solution.add(getSolution(level, recipe));
		});
		
		List<ItemStack> preparedIngredients = ingredients.stream()
				.flatMap((Function<ItemStack, Stream<ItemStack>>) itemStack -> {
					return prepareIngredientStack(itemStack, level, random, config).stream();
				})
				.collect(Collectors.toList());
		
		Collections.shuffle(preparedIngredients, random);
		
		LOGGER.info("Generated CraftingPuzzle with solution: {}",
				solution.stream().map(ItemStack::toString).collect(Collectors.joining(", ")));
		
		return new CraftingPuzzle(preparedIngredients, solution);
	}
	
	private static List<ItemStack> prepareIngredientStack(ItemStack rawIngredientStack, Level level, Random random, PuzzleType config) {
		List<ItemStack> results = new ArrayList<>();
		
		results.add(rawIngredientStack);
		
		while (random.nextFloat() <= config.splitProbability) {
			Function<ItemStack, Stream<ItemStack>> stackSplitter = itemStack -> {
				return splitStack(itemStack, level, random, config);
			};
			
			results = results.stream().flatMap(stackSplitter).collect(Collectors.toList());
		}
		
		return results;
	}
	
	private static Stream<ItemStack> splitStack(ItemStack itemStack, Level level, Random random, PuzzleType config) {
		if (random.nextFloat() > config.splitProbability) {
			return Stream.of(itemStack);
		}
		
		List<CraftingRecipe> recipes = getRecipesFor(itemStack, level);
		
		if (recipes.size() == 0) {
			return Stream.of(itemStack);
		}
		
		CraftingRecipe recipe = recipes.get(random.nextInt(recipes.size()));
		
		return recipe.getIngredients().stream()
				.filter(CraftingPuzzle::isNonDummyIngredient)
				.map(ingredient -> getRandomIngredient(ingredient, random));
	}
	
	private static List<CraftingRecipe> getRecipesFor(ItemStack item, Level level) {
		RecipeManager recipeManager = level.getRecipeManager();
		
		return recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
				.stream()
				.filter(recipe -> {
					ItemStack output = recipe.getResultItem(level.registryAccess());
					return output.sameItem(item) && output.getCount() <= item.getCount();
				})
				.collect(Collectors.toList());
	}
	
	private static List<CraftingRecipe> getRandomRecipes(Level level, Random random, PuzzleType config) {
		List<CraftingRecipe> recipes = getRecipesForConfig(level, config);
		List<CraftingRecipe> randomRecipes = getRandomRecipesWeighted(level, random, config, recipes);
		
		if (randomRecipes == null) {
			randomRecipes = getRandomRecipesSafe(random, config, recipes);
		}
		
		return randomRecipes;
	}
	
	private static List<CraftingRecipe> getRandomRecipesWeighted(Level level, Random random, PuzzleType config, List<CraftingRecipe> recipes) {
		List<CraftingRecipe> randomRecipes = new ArrayList<>();
		
		for (int i = 0; i < config.solutionSize; i++) {
			CraftingRecipe randomRecipe;
			
			int limit = 1000;
			do {
				randomRecipe = recipes.get(random.nextInt(recipes.size()));
				
				if (--limit < 0) {
					return null;
				}
				
			} while (randomRecipes.contains(randomRecipe) || random.nextFloat() > getRecipeWeight(level, randomRecipe));
			
			randomRecipes.add(randomRecipe);
		}
		
		return randomRecipes;
	}
	
	private static List<CraftingRecipe> getRandomRecipesSafe(Random random, PuzzleType config, List<CraftingRecipe> recipes) {
		List<CraftingRecipe> recipesCopy = new ArrayList<>(recipes);
		
		Collections.shuffle(recipesCopy, random);
		
		return recipesCopy.subList(0, config.solutionSize);
	}
	
	private static float getRecipeWeight(Level level, CraftingRecipe recipe) {
		List<ITag<?>> usedTags = new ArrayList<>();
		
		ITagManager<Item> itemTags = ForgeRegistries.ITEMS.tags();
		
		if (itemTags != null) {
			for (TagKey<Item> tagKey : filterTags(itemTags.getTagNames())) {
				ITag<Item> tag = itemTags.getTag(tagKey);
				if (isRecipeUsesItemTag(level, recipe, tag)) {
					usedTags.add(tag);
				}
			}
		}
		
		ITagManager<Block> blockTags = ForgeRegistries.BLOCKS.tags();
		
		if (blockTags != null) {
			for (TagKey<Block> tagKey : filterTags(blockTags.getTagNames())) {
				ITag<Block> tag = blockTags.getTag(tagKey);
				if (isRecipeUsesBlockTag(level, recipe, tag)) {
					usedTags.add(tag);
				}
			}
		}
		
		OptionalInt highestTagSize = usedTags.stream()
				.mapToInt(ITag::size)
				.max();
		
		return 1f / highestTagSize.orElse(1);
	}
	
	private static <T, I extends TagKey<T>> List<I> filterTags(Stream<I> tagsStream) {
		return tagsStream
				.filter(tag -> "minecraft".equals(tag.location().getNamespace()))
				.collect(Collectors.toList());
	}
	
	private static boolean isRecipeUsesItemTag(Level level, CraftingRecipe recipe, ITag<Item> tag) {
		Predicate<ItemStack> predicate = itemStack -> tag.contains(itemStack.getItem());
		
		return isRecipeResultMatches(level, recipe, predicate)
				|| anyRecipeIngredientMatches(recipe, predicate);
	}
	
	private static boolean isRecipeUsesBlockTag(Level level, CraftingRecipe recipe, ITag<Block> tag) {
		Predicate<ItemStack> predicate = itemStack -> {
			Item item = itemStack.getItem();
			
			return item instanceof BlockItem
					&& tag.contains(((BlockItem) item).getBlock());
		};
		
		return isRecipeResultMatches(level, recipe, predicate)
				|| anyRecipeIngredientMatches(recipe, predicate);
	}
	
	private static boolean isRecipeResultMatches(Level level, CraftingRecipe recipe, Predicate<ItemStack> predicate) {
		return predicate.test(recipe.getResultItem(level.registryAccess()));
	}
	
	private static boolean anyRecipeIngredientMatches(CraftingRecipe recipe, Predicate<ItemStack> predicate) {
		return recipe.getIngredients().stream().anyMatch(ingredient -> {
			return Arrays.stream(ingredient.getItems()).anyMatch(predicate);
		});
	}
	
	@Nonnull
	private static List<CraftingRecipe> getRecipesForConfig(Level level, PuzzleType config) {
		if (!RECIPES_CACHE.containsKey(config)) {
			RecipeManager recipeManager = level.getRecipeManager();
			
			List<CraftingRecipe> recipes = recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
					.stream()
					.filter(recipe -> isValidRecipe(level, recipe, config))
					.collect(Collectors.toList());
			
			RECIPES_CACHE.put(config, recipes);
			
			recipes.forEach(r -> LOGGER.info(r.getResultItem(level.registryAccess())));
		}
		
		return RECIPES_CACHE.get(config);
	}
	
	private static boolean isValidRecipe(Level level, CraftingRecipe recipe, PuzzleType config) {
		return isRecipeOutputValid(level, recipe, config)
				&& isRecipeIngredientsValid(recipe, config);
	}
	
	private static boolean isRecipeIngredientsValid(CraftingRecipe recipe, PuzzleType config) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		int totalIngredients = ingredients.size();
		
		if (!config.ingredientRange.test(totalIngredients)) {
			return false;
		}
		
		int uniqueIngredients = ingredients.stream()
				.filter(CraftingPuzzle::isNonDummyIngredient)
				.collect(Collectors.groupingBy(ingredient -> {
					return listToSet(Arrays.asList(ingredient.getItems()));
				}))
				.size();
		
		return config.uniqueIngredientRange.test(uniqueIngredients);
	}
	
	private static boolean isRecipeOutputValid(Level level, CraftingRecipe recipe, PuzzleType config) {
		ItemStack recipeOutput = recipe.getResultItem(level.registryAccess());
		
		return config.recipeOutputStackRange.test(recipeOutput.getCount())
				&& "minecraft".equals(recipe.getId().getNamespace())
				&& !BLACKLIST.contains(ForgeRegistries.ITEMS.getKey(recipeOutput.getItem()));
	}
	
	private static Set<Equivalence.Wrapper<ItemStack>> listToSet(List<ItemStack> itemStacks) {
		return itemStacks.stream().map(ITEM_STACK_EQUIVALENCE::wrap).collect(Collectors.toSet());
	}
	
	private static boolean isNonDummyIngredient(Ingredient ingredient) {
		return ingredient.getItems().length > 0;
	}
	
	private static ItemStack getRandomIngredient(Ingredient ingredient, Random random) {
		ItemStack[] itemStacks = ingredient.getItems();
		ItemStack itemStack = itemStacks[random.nextInt(itemStacks.length)];
		
		return itemStack.copy();
	}
	
	private static ItemStack getSolution(Level level, CraftingRecipe recipe) {
		return recipe.getResultItem(level.registryAccess()).copy();
	}
	
}
