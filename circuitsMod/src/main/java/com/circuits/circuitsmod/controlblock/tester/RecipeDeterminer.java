package com.circuits.circuitsmod.controlblock.tester;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import jdk.nashorn.internal.runtime.Debug;

import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.common.SerializableItemStack;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;
import com.sun.corba.se.impl.orbutil.graph.Graph;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.toposort.TopologicalSort.DirectedGraph;

public class RecipeDeterminer {
	
	private static int costCurve(int x) {
		return (int) Math.sqrt((float) x);
		
	}
	
	//TODO: Should we instead write out separate recipes for every specialization?
	//if so, how would that interact with the inductive principle for unlocks?
	private static void writeRecipeOut(CircuitUID uid, List<ItemStack> cost) throws IOException {
		
		Optional<File> folder = Common.getCircuitFolder(circuitName);
		if (!folder.isPresent()) {
			Log.internalError("Circuit folder not present when determining recipe! " + uid + " " + cost.toString());
			Log.internalError("Failed to write out circuit crafting recipe");
			return;
		}
		SerializableItemStack.itemStacksToFile(cost, new File(folder.get().toPath() + "/materials.nbt"));
	}
	
	public static Item itemFromState(IBlockState in) {
		return Optional.ofNullable(Item.getItemFromBlock(in.getBlock()))
	             .orElseGet(() -> in.getBlock().getItemDropped(in, ThreadLocalRandom.current(), 0));
	}
	
	public static void determineRecipe(final Tester test) {
		
		final RecipeGraph.CostList cost = new RecipeGraph.CostList();
		
		PosUtils.forBlockIn(test.getWorld(), test.getBBox().contract(1.0), (IBlockState in) -> {
			int meta = in.getBlock().getMetaFromState(in);
			Item item = itemFromState(in);
			
			System.out.println(in.getBlock());
			
			RecipeGraph.CostList addedCost = null;
			if (item != null) {
				addedCost = Microchips.recipeGraph.getCost(new RecipeGraph.ItemData(item, meta));
			}	
			if (addedCost != null) {
				cost.mergeCost(addedCost);
			}
		});
		
		List<ItemStack> costStacks = ItemUtils.mapOverQty(cost.extractItemStack(), RecipeDeterminer::costCurve);
		
		costStacks = ItemUtils.sortQty(costStacks);

		costStacks = costStacks.stream().limit(6).collect(Collectors.toCollection(ArrayList::new));
		
		try {
			writeRecipeOut(test.circuitUID.getUID(), costStacks);
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
}
