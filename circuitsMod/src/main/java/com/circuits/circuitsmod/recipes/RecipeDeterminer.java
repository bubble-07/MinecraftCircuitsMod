package com.circuits.circuitsmod.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.Config;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.tester.Tester;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipeDeterminer {
	
	public static enum CostCurve {
		CONSTANT("constant", (x) -> 1.0, (y) -> 1.0),
		LINEAR("linear", (x) -> x, (y) -> y),
		SQRT("sqrt", (x) -> Math.sqrt(x), (y) -> Math.pow(y, 2)),
		LOG("log", (x) -> Math.log1p(x), (y) -> (Math.exp(y) - 1));
		
		private final String name;
		private final Function<Double, Double> costComputation;
		private final Function<Double, Double> costComputationInverse;
		CostCurve(String name, Function<Double, Double> costComputation, Function<Double, Double> costComputationInverse) {
			this.name = name;
			this.costComputation = costComputation;
			this.costComputationInverse = costComputationInverse;
		}
		public String getName() {
			return this.name;
		}
		
		public double computeCost(double inputVal) {
			return costComputation.apply(inputVal);
		}
		public double inverse(double inputVal) {
			return costComputationInverse.apply(inputVal);
		}
		
	}
	
	public static Item itemFromState(IBlockState in) {
		return Optional.ofNullable(Item.getItemFromBlock(in.getBlock()))
	             .orElseGet(() -> in.getBlock().getItemDropped(in, ThreadLocalRandom.current(), 0));
	}
	
	public static int costFromQty(int qty) {
		return (int) (double) Config.circuitCostCurve.costComputation.apply((double) qty);
	}
	
	public static void determineRecipe(final Tester test) {
		
		final RecipeGraph.CostList cost = new RecipeGraph.CostList();
		
		PosUtils.forBlockIn(test.getWorld(), test.getBBox(), (IBlockState in) -> {
			int meta = in.getBlock().getMetaFromState(in);
			Item item = itemFromState(in);
						
			RecipeGraph.CostList addedCost = null;
			if (item != null && item != StartupCommonFrame.itemFrameBlock) {
				addedCost = CircuitsMod.recipeGraph.getCost(new RecipeGraph.ItemData(item, meta));
			}	
			if (addedCost != null) {
				cost.mergeCost(addedCost);
			}
		});
		
		List<ItemStack> costStacks = ItemUtils.mapOverQty(cost.extractItemStack(), RecipeDeterminer::costFromQty);
		
		costStacks = ItemUtils.sortQty(costStacks);

		costStacks = costStacks.stream().limit(6).collect(Collectors.toCollection(ArrayList::new));
		
		
		try {
			RecipeUtils.writeRecipeOut(test.getInvokingPlayer(), test.getUID().getUID(), costStacks);
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
}
