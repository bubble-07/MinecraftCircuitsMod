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

/**
 * Represents the graph of Item + Metadata pairs with an edge for every
 * crafting recipe dependency. For ores, the Item is taken to be the first
 * Item found in the ore dictionary, which means that if you use this,
 * you should probably make account for ores manually!
 * @author bubble-07
 *
 */
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
			this.item = in.getItem();
			this.data = in.getMetadata();
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
	
	HashMap<ItemData, Node> itemMap = new HashMap<>();
	
	//Gets a node, or creates if needed
	private Node getNode(ItemData in) {
		itemMap.putIfAbsent(in, new Node(in));
		return itemMap.get(in);
	}
	
	private void constructHelper(ItemStack output, Collection<ItemStack> inputs) {

		Node source = getNode(new ItemData(output));
		
		List<ItemStack> summedCost = (new CostList().addItemStacks(inputs, 1.0f)).extractItemStack();

		for (ItemStack input : summedCost) {
			if (input != null && input.stackSize > 0) {
				Node dest = getNode(new ItemData(input));
				float edgeWeight = ((float) input.stackSize) / ((float) output.stackSize);
				source.indegree++;
				source.addEdge(new Edge(edgeWeight, dest));
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

	private static List<ItemStack> fromOreRecipeInput(Collection<Object> inputs) {
		List<ItemStack> stacks = new ArrayList<>();
		for (Object input : inputs) {
			if (input != null) {
				if (input instanceof ItemStack) {
					stacks.add((ItemStack) input);
				}
				else if (input instanceof List) {
					//Just add the first ore in the ore list as the representative for the ore group
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
	public CostList getCost(Node node, float preMult) {
		CostList ret = new CostList();
		
		if (node == null) {
			return ret;
		}
		
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
			for (Edge edge : node.edges) {
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
	
	private static class Node {
		ItemData item;
		ArrayList<Edge> edges;
		int indegree = 0;
		public boolean visited;
		private Node(ItemData item, ArrayList<Edge> edges) {
			this.item = item;
			this.edges = edges;
			this.visited = false;
		}
		public Node(ItemData item) {
			this(item, new ArrayList<>());
		}
		public Node addEdge(Edge toAdd) {
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
			Node other = (Node) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			return true;
		}
		
	}
	
	private static class Edge {
		float numRequired;
		Node dest;
		public Edge(float numRequired, Node dest) {
			this.numRequired = numRequired;
			this.dest = dest;
		}
	}
	
}
