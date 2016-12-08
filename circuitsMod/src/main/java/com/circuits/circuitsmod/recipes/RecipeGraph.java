package com.circuits.circuitsmod.recipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

//TODO: Expand the "ItemData" here to incorporate circuits with given ids
//TODO: Un-reify this, so that you can account for the difference in circuit costs between players.
public class RecipeGraph {
	
	//Item, metadata pair
	public static class ItemData {
		public Item item;
		public int data;
		public ItemData(Item item, int data) {
			this.item = item;
			this.data = data;
		}
		public ItemData(ItemStack in) {
			this(in.getItem(), in.getMetadata());
		}
		public ItemStack toStack(int num) {
			return new ItemStack(this.item, num, this.data);
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + data;
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ItemData other = (ItemData) obj;
			if (data != other.data)
				return false;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}
		
		
	}
	
	
	HashMap<ItemData, RecipeNode> itemMap = new HashMap<>();
	
	//Gets a node, or creates if needed
	private RecipeNode getNode(ItemData in) {
		RecipeNode result = itemMap.get(in);
		if (result == null) {
			result = new RecipeNode(in);
			itemMap.put(in, result);
			//System.out.println(in.item.getUnlocalizedName());
		}
		return result;
	}
	
	private void constructHelper(ItemStack output, Collection<ItemStack> inputs) {

		RecipeNode source = getNode(new ItemData(output));
		
		List<ItemStack> summedCost = (new CostList().addItemStacks(inputs, 1.0f)).extractItemStack();

		for (ItemStack input : summedCost) {
			if (input != null && input.stackSize > 0) {
				RecipeNode dest = getNode(new ItemData(input));
				float edgeWeight = ((float) input.stackSize) / ((float) output.stackSize);
				source.indegree++;
				source.addEdge(new RecipeEdge(edgeWeight, dest));
			}
		}
	}
	
	public RecipeGraph() {
		//Construct the recipe graph
		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
		
		for (IRecipe recipe : recipes) {
			if (recipe instanceof ShapedRecipes) {
				ShapedRecipes shaped = (ShapedRecipes) recipe;
				constructHelper(shaped.getRecipeOutput(), Arrays.asList(shaped.recipeItems));
			}
			else if (recipe instanceof ShapelessRecipes) {
				ShapelessRecipes shapeless = (ShapelessRecipes) recipe;
				constructHelper(shapeless.getRecipeOutput(), shapeless.recipeItems);
			}
			else if (recipe instanceof ShapedOreRecipe) {
				ShapedOreRecipe shapedore = (ShapedOreRecipe) recipe;
				List<ItemStack> inputs = fromOreRecipeInput(Arrays.asList(shapedore.getInput()));
				
				constructHelper(shapedore.getRecipeOutput(), inputs);
			}
			else if (recipe instanceof ShapelessOreRecipe) {
				ShapelessOreRecipe shapelessore = (ShapelessOreRecipe) recipe;
				
				constructHelper(shapelessore.getRecipeOutput(), fromOreRecipeInput(shapelessore.getInput()));
			}
			else {
				//System.out.println(recipe.getClass());
			}
		}
	}
	
	//TODO: Allow ore substitutions in your crafting interface!
	private static List<ItemStack> fromOreRecipeInput(Collection<Object> inputs) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Object input : inputs) {
			if (input != null) {
				if (input instanceof ItemStack) {
					stacks.add((ItemStack) input);
				}
				else if (input instanceof List) {
					//Just add the first ore in the ore list
					//TODO: Maybe less suckage?
					List<ItemStack> in = (List<ItemStack>) input;
					stacks.add(in.get(0));
				}
			}
		}
		return stacks;
	}
	
	public static class CostList {
		private Map<ItemData, Float> costs = new HashMap<>();
		private List<ItemData> usedItems = new ArrayList<>();
		public CostList() { }
		
		public CostList addItem(ItemData toAdd, float cost) {
			if (costs.containsKey(toAdd)) {
				float priorCost = costs.get(toAdd);
				costs.put(toAdd, priorCost + cost);
			}
			else {
				costs.put(toAdd, cost);
				usedItems.add(toAdd);
			}
			return this;
		}
		
		public CostList mergeCost(CostList other) {
			for (ItemData item : other.usedItems) {
				this.addItem(item, other.costs.get(item));
			}
			return this;
		}
		
		public CostList addItemStack(ItemStack toAdd, float multiplier) {
			addItem(new ItemData(toAdd), multiplier * toAdd.stackSize);
			return this;
		}
		public CostList addItemStacks(Collection<ItemStack> toAdd, float multiplier) {
			for (ItemStack item : toAdd) {
				if (item != null) {
					addItemStack(item, multiplier);
				}
			}
			return this;
		}
		
		//Returns a representation of this as an itemstack
		public List<ItemStack> extractItemStack() {
			List<ItemStack> result = new ArrayList<>();
			for (ItemData item : usedItems) {
				result.add(item.toStack((int)Math.ceil(costs.get(item))));
			}
			return result;
		}
	}
	
	public CostList getCost(ItemData item) {
		return getCost(itemMap.get(item), 1.0f);
	}
	
	//Does a depth-first traversal to determine the cost of a recipe node
	public CostList getCost(RecipeNode node, float preMult) {
		CostList ret = new CostList();
		
		if (node == null) {
			return ret;
		}
		
		//System.out.println(node.item.item.getUnlocalizedName());
		//System.out.println(node.edges.size());
		
		node.visited = true;
		
		if (node.edges.size() == 0) {
			//Must be a base item
			ret.addItem(node.item, preMult);
		}
		else if (Arrays.asList(new String[]{"tile.cloth", "tile.clay", "item.redstone"}).contains(
				node.item.item.getUnlocalizedName())) {
			//Do not recurse! Bad things happen with dyed cloth/clay and redstone!
			ret.addItem(node.item, preMult);
		}
		else {		
			for (RecipeEdge edge : node.edges) {
				if (edge.dest.visited == true) {
					//Must be exploring a cycle!
					//To resolve it, pick the one with the higher indegree
					//(Inspiration: iron ingots vs. block of iron situation)
					//TODO: I can't figure this out for the life of me, but there's a block of redstone
					//with __different__ metadata in the recipes table. 
					//System.out.println("This indeg: " + node.indegree + "Other indeg: " + edge.dest.indegree);
					if (node.indegree > edge.dest.indegree) {
						//Use the node
						ret.addItem(node.item, preMult);
					}
					else {
						ret.addItem(edge.dest.item, preMult * edge.numRequired);
					}
					
				}
				else {
					//System.out.println("    " + edge.dest.item.item.getUnlocalizedName());
					//Otherwise, we're probly good
					ret.mergeCost(getCost(edge.dest, preMult * edge.numRequired));
				}
			}
		}
		
		node.visited = false;
		return ret;
	}
	
	public static class RecipeNode {
		ItemData item;
		ArrayList<RecipeEdge> edges;
		int indegree = 0;
		public boolean visited;
		private RecipeNode(ItemData item, ArrayList<RecipeEdge> edges) {
			this.item = item;
			this.edges = edges;
			this.visited = false;
		}
		public RecipeNode(ItemData item) {
			this(item, new ArrayList<>());
		}
		public RecipeNode addEdge(RecipeEdge toAdd) {
			this.edges.add(toAdd);
			return this;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((item == null) ? 0 : item.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RecipeNode other = (RecipeNode) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}
		
	}
	
	public static class RecipeEdge {
		float numRequired;
		RecipeNode dest;
		public RecipeEdge(float numRequired, RecipeNode dest) {
			this.numRequired = numRequired;
			this.dest = dest;
		}
	}
	
}
