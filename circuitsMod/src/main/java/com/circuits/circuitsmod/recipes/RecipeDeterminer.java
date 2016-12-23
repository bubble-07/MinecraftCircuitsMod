package com.circuits.circuitsmod.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;




import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.Config;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.tester.Tester;




import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

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
	
	public static int costFromQty(int qty) {
		int result = (int) (double) Config.circuitCostCurve.costComputation.apply((double) qty);
		//Cool, now ensure that it's >= 1
		return result >= 1 ? result : 1;
	}
	
	/**
	 * From the "raw" recipe (which may include arbitrarily-many items and represents the
	 * recursive sum of costs) saved *somewhere*, get the recipe as it will appear in the GUI
	 * @param worldIn
	 * @param playerID
	 * @param uid
	 * @return
	 */
	public static Optional<List<ItemStack>> getRecipeFor(World worldIn, UUID playerID, CircuitUID uid) {
		Optional<List<ItemStack>> stacks = RecipeUtils.getRawRecipeFor(worldIn, playerID, uid);
		return stacks.map(RecipeDeterminer::curateItemStacks);
	}
	
	private static List<ItemStack> curateItemStacks(List<ItemStack> stacks) {
		List<ItemStack> costStacks = ItemUtils.mapOverQty(stacks, RecipeDeterminer::costFromQty);

		costStacks = ItemUtils.sortQty(costStacks);

		costStacks = costStacks.stream().limit(6).collect(Collectors.toCollection(ArrayList::new));
		return costStacks;
	}
	
	public static <T extends TileEntity> void determineRecipe(final Tester<T> test) {
		
		final RecipeGraph.CostList cost = new RecipeGraph.CostList();
		
		PosUtils.forBlockPosIn(test.getWorld(), test.getBBox(), (pos) -> {
			Optional<List<ItemStack>> stacks = RecipeCrawler.getCostFor(test.getWorld(), pos, test.getInvokingPlayer().getUniqueID());
			if (stacks.isPresent()) {
				cost.addItemStacks(stacks.get(), 1.0f);
			}
		});
		
		List<ItemStack> costStacks = cost.extractItemStack();
		
		try {
			RecipeUtils.writeRawRecipeOut(test.getInvokingPlayer(), test.getUID().getUID(), costStacks);
		}
		catch (IOException e) {
			System.err.println(e);
		}
	}
}
