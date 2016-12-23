package com.circuits.circuitsmod.recipes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Collection of utility methods for determining recipes from
 * blocks. 
 * @author bubble-07
 *
 */
public class RecipeCrawler {
	public static Optional<List<ItemStack>> getCostFor(World worldIn, BlockPos pos, UUID playerID) {
		Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(worldIn, pos);
		if (te.isPresent()) {
			return getCostForCircuit(worldIn, te.get().getCircuitUID(), playerID);
		}
		Optional<ItemStack> stack = RecipeUtils.unitItemStackFromState(worldIn.getBlockState(pos));
		return stack.map(RecipeCrawler::getCostForUnitItemStack);
	}
	public static Optional<List<ItemStack>> getCostForCircuit(World worldIn, SpecializedCircuitUID circuit, UUID playerID) {
		return RecipeUtils.getRawRecipeFor(worldIn, playerID, circuit.getUID());
	}
	public static List<ItemStack> getCostForUnitItemStack(ItemStack stack) {
		RecipeGraph.CostList costs = CircuitsMod.recipeGraph.getCost(new RecipeGraph.ItemData(stack));
		return costs.extractItemStack();
		
	}
}
