package ru.qoqqi.qcraft.puzzles;

import com.google.common.base.Equivalence;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagRegistry;
import net.minecraft.tags.TagRegistryManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	
	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	private static final List<ResourceLocation> BLACKLIST = Arrays.asList(
			new ResourceLocation("minecraft:filled_map")
	);
	
	private static final Map<PuzzleType, List<ICraftingRecipe>> RECIPES_CACHE = new HashMap<>();
	
	private static final Equivalence<ItemStack> ITEM_STACK_EQUIVALENCE = new Equivalence<ItemStack>() {
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
	
	public static CraftingPuzzle generate(World world, Random random, PuzzleType config) {
		List<ICraftingRecipe> recipes = getRandomRecipes(world, random, config);
		
		List<ItemStack> ingredients = new ArrayList<>();
		List<ItemStack> solution = new ArrayList<>();
		
		recipes.forEach(recipe -> {
			recipe.getIngredients().stream()
					.filter(CraftingPuzzle::isNonDummyIngredient)
					.forEach(ingredient -> {
						ingredients.add(getRandomIngredient(ingredient, random));
					});
			
			solution.add(getSolution(recipe));
		});
		
		List<ItemStack> preparedIngredients = ingredients.stream()
				.flatMap((Function<ItemStack, Stream<ItemStack>>) itemStack -> {
					return prepareIngredientStack(itemStack, world, random, config).stream();
				})
				.collect(Collectors.toList());
		
		Collections.shuffle(preparedIngredients, random);
		
		LOGGER.info("Generated CraftingPuzzle with solution: {}",
				solution.stream().map(ItemStack::toString).collect(Collectors.joining(", ")));
		
		return new CraftingPuzzle(preparedIngredients, solution);
	}
	
	private static List<ItemStack> prepareIngredientStack(ItemStack rawIngredientStack, World world, Random random, PuzzleType config) {
		List<ItemStack> results = new ArrayList<>();
		
		results.add(rawIngredientStack);
		
		while (random.nextFloat() <= config.splitProbability) {
			Function<ItemStack, Stream<ItemStack>> stackSplitter = itemStack -> {
				return splitStack(itemStack, world, random, config);
			};
			
			results = results.stream().flatMap(stackSplitter).collect(Collectors.toList());
		}
		
		return results;
	}
	
	private static Stream<ItemStack> splitStack(ItemStack itemStack, World world, Random random, PuzzleType config) {
		if (random.nextFloat() > config.splitProbability) {
			return Stream.of(itemStack);
		}
		
		List<ICraftingRecipe> recipes = getRecipesFor(itemStack, world);
		
		if (recipes.size() == 0) {
			return Stream.of(itemStack);
		}
		
		ICraftingRecipe recipe = recipes.get(random.nextInt(recipes.size()));
		
		return recipe.getIngredients().stream()
				.filter(CraftingPuzzle::isNonDummyIngredient)
				.map(ingredient -> getRandomIngredient(ingredient, random));
	}
	
	private static List<ICraftingRecipe> getRecipesFor(ItemStack item, World world) {
		RecipeManager recipeManager = world.getRecipeManager();
		
		return recipeManager.getRecipesForType(IRecipeType.CRAFTING)
				.stream()
				.filter(recipe -> {
					ItemStack output = recipe.getRecipeOutput();
					return output.isItemEqual(item) && output.getCount() <= item.getCount();
				})
				.collect(Collectors.toList());
	}
	
	private static List<ICraftingRecipe> getRandomRecipes(World world, Random random, PuzzleType config) {
		List<ICraftingRecipe> recipes = getRecipesForConfig(world, config);
		List<ICraftingRecipe> randomRecipes = getRandomRecipesWeighted(random, recipes);
		
		if (randomRecipes == null) {
			randomRecipes = getRandomRecipesSafe(random, recipes);
		}
		
		return randomRecipes;
	}
	
	private static List<ICraftingRecipe> getRandomRecipesWeighted(Random random, List<ICraftingRecipe> recipes) {
		List<ICraftingRecipe> randomRecipes = new ArrayList<>();
		
		for (int i = 0; i < 3; i++) {
			ICraftingRecipe randomRecipe;
			
			int limit = 1000;
			do {
				randomRecipe = recipes.get(random.nextInt(recipes.size()));
				
				if (--limit < 0) {
					return null;
				}
				
			} while (randomRecipes.contains(randomRecipe) || random.nextFloat() > getRecipeWeight(randomRecipe));
			
			randomRecipes.add(randomRecipe);
		}
		
		return randomRecipes;
	}
	
	private static List<ICraftingRecipe> getRandomRecipesSafe(Random random, List<ICraftingRecipe> recipes) {
		List<ICraftingRecipe> recipesCopy = new ArrayList<>(recipes);
		
		Collections.shuffle(recipesCopy, random);
		
		return recipesCopy.subList(0, 3);
	}
	
	private static float getRecipeWeight(ICraftingRecipe recipe) {
		List<ITag<?>> usedTags = new ArrayList<>();
		
		for (ITag.INamedTag<Item> tag : ItemTags.getAllTags()) {
			if (isRecipeUsesItemTag(recipe, tag)) {
				usedTags.add(tag);
			}
		}
		
		for (ITag.INamedTag<Block> tag : BlockTags.getAllTags()) {
			if (isRecipeUsesBlockTag(recipe, tag)) {
				usedTags.add(tag);
			}
		}
		
		OptionalInt highestTagSize = usedTags.stream()
				.mapToInt(tag -> tag.getAllElements().size())
				.max();
		
		return 1f / highestTagSize.orElse(1);
	}
	
	private static boolean isRecipeUsesItemTag(ICraftingRecipe recipe, ITag<Item> tag) {
		Predicate<ItemStack> predicate = itemStack -> tag.contains(itemStack.getItem());
		
		return isRecipeResultMatches(recipe, predicate)
				|| anyRecipeIngredientMatches(recipe, predicate);
	}
	
	private static boolean isRecipeUsesBlockTag(ICraftingRecipe recipe, ITag<Block> tag) {
		Predicate<ItemStack> predicate = itemStack -> {
			Item item = itemStack.getItem();
			
			return item instanceof BlockItem
					&& tag.contains(((BlockItem) item).getBlock());
		};
		
		return isRecipeResultMatches(recipe, predicate)
				|| anyRecipeIngredientMatches(recipe, predicate);
	}
	
	private static boolean isRecipeResultMatches(ICraftingRecipe recipe, Predicate<ItemStack> predicate) {
		return predicate.test(recipe.getRecipeOutput());
	}
	
	private static boolean anyRecipeIngredientMatches(ICraftingRecipe recipe, Predicate<ItemStack> predicate) {
		return recipe.getIngredients().stream().anyMatch(ingredient -> {
			return Arrays.stream(ingredient.getMatchingStacks()).anyMatch(predicate);
		});
	}
	
	@Nonnull
	private static List<ICraftingRecipe> getRecipesForConfig(World world, PuzzleType config) {
		if (!RECIPES_CACHE.containsKey(config)) {
			RecipeManager recipeManager = world.getRecipeManager();
			
			List<ICraftingRecipe> recipes = recipeManager.getRecipesForType(IRecipeType.CRAFTING)
					.stream()
					.filter(recipe -> isValidRecipe(recipe, config))
					.collect(Collectors.toList());
			
			RECIPES_CACHE.put(config, recipes);
			
			recipes.forEach(r -> System.out.println(r.getRecipeOutput()));
		}
		
		return RECIPES_CACHE.get(config);
	}
	
	private static boolean isValidRecipe(ICraftingRecipe recipe, PuzzleType config) {
		return isRecipeOutputValid(recipe, config)
				&& isRecipeIngredientsValid(recipe, config);
	}
	
	private static boolean isRecipeIngredientsValid(ICraftingRecipe recipe, PuzzleType config) {
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		int totalIngredients = ingredients.size();
		
		if (!config.ingredientRange.test(totalIngredients)) {
			return false;
		}
		
		int uniqueIngredients = ingredients.stream()
				.filter(CraftingPuzzle::isNonDummyIngredient)
				.collect(Collectors.groupingBy(ingredient -> {
					return listToSet(Arrays.asList(ingredient.getMatchingStacks()));
				}))
				.size();
		
		return config.uniqueIngredientRange.test(uniqueIngredients);
	}
	
	private static boolean isRecipeOutputValid(ICraftingRecipe recipe, PuzzleType config) {
		ItemStack recipeOutput = recipe.getRecipeOutput();
		
		return config.recipeOutputStackRange.test(recipeOutput.getCount())
				&& "minecraft".equals(recipe.getId().getNamespace())
				&& !BLACKLIST.contains(recipeOutput.getItem().getRegistryName());
	}
	
	private static Set<Equivalence.Wrapper<ItemStack>> listToSet(List<ItemStack> itemStacks) {
		return itemStacks.stream().map(ITEM_STACK_EQUIVALENCE::wrap).collect(Collectors.toSet());
	}
	
	private static boolean isNonDummyIngredient(Ingredient ingredient) {
		return ingredient.getMatchingStacks().length > 0;
	}
	
	private static ItemStack getRandomIngredient(Ingredient ingredient, Random random) {
		ItemStack[] itemStacks = ingredient.getMatchingStacks();
		ItemStack itemStack = itemStacks[random.nextInt(itemStacks.length)];
		
		return itemStack.copy();
	}
	
	private static ItemStack getSolution(ICraftingRecipe recipe) {
		return recipe.getRecipeOutput().copy();
	}
	
}
