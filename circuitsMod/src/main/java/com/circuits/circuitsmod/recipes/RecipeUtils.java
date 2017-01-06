package com.circuits.circuitsmod.recipes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.circuits.circuitsmod.Config;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.PersistentCircuitUIDs;
import com.circuits.circuitsmod.common.EntityUtils;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerializableItemStack;
import com.google.common.collect.Lists;

public class RecipeUtils {

	//TODO: Should we instead write out separate recipes for every specialization?
	//if so, how would that interact with the inductive principle for unlocks?
	/**
	 * Writes the recipe for
	 * @param player
	 * @param uid
	 * @param cost
	 * @throws IOException
	 */
	public static void writeRawRecipeOut(EntityPlayer player, CircuitUID uid, List<ItemStack> cost) throws IOException {
		File recipeFile = getRecipeFileFor(player, uid);
		SerializableItemStack.itemStacksToFile(cost, recipeFile);
	}
	
	private static File getRecipeFileFor(EntityPlayer player, CircuitUID uid) throws IOException {
		File worldSavesDir = FileUtils.getWorldRecipesDir();
		File destFile = null;
		if (Config.isCircuitsProgressWorldGlobal) {
			destFile = new File(worldSavesDir + "/" + uid.toInteger() + "materials.nbt");
		}
		else {
			File playerFolder = new File(worldSavesDir + "/" + player.getName());
			playerFolder.mkdirs();
			destFile = new File(playerFolder + "/" + uid.toInteger() + "materials.nbt");
		}
		return destFile;
	}
	
	//Costs for circuits that are fixed (like the ADC/DAC) because they're just a pain to build,
	//or for things like inputs/outputs/combiners/splitters which are impossible to build (primitives),
	//or for things that are entirely trivial (like delay circuits)
	public static HashMap<CircuitUID, List<ItemStack>> persistentCosts = new HashMap<>();
	static {
		addCost(PersistentCircuitUIDs.INPUT_CIRCUIT, new ItemStack(Items.REDSTONE, 1), new ItemStack(Blocks.STONE, 1));
		addCost(PersistentCircuitUIDs.OUTPUT_CIRCUIT, new ItemStack(Items.REDSTONE, 1), new ItemStack(Blocks.STONE, 1));
		addCost(PersistentCircuitUIDs.ADC_CIRCUIT, new ItemStack(Items.REDSTONE, 4), new ItemStack(Items.QUARTZ, 1), new ItemStack(Blocks.STONE, 4));
		addCost(PersistentCircuitUIDs.DAC_CIRCUIT, new ItemStack(Items.REDSTONE, 4), new ItemStack(Items.QUARTZ, 1), new ItemStack(Blocks.STONE, 4));
		addCost(PersistentCircuitUIDs.SPLITTER_CIRCUIT, new ItemStack(Items.REDSTONE, 3));
		addCost(PersistentCircuitUIDs.COMBINER_CIRCUIT, new ItemStack(Items.REDSTONE, 3));
		addCost(PersistentCircuitUIDs.INPUT_SELECT_CIRCUIT, new ItemStack(Items.REDSTONE, 2));
		addCost(PersistentCircuitUIDs.OUTPUT_SELECT_CIRCUIT, new ItemStack(Items.REDSTONE, 2));
		addCost(PersistentCircuitUIDs.DELAY_CIRCUIT, new ItemStack(Items.REDSTONE, 3), new ItemStack(Blocks.STONE, 3));
		//TODO: Are clocks non-trivial? Whatever the case, test is broken for them, so putting them here for now :P
		addCost(PersistentCircuitUIDs.CLOCK_CIRCUIT, new ItemStack(Items.REDSTONE, 6), new ItemStack(Blocks.STONE, 6));

	}
	
	private static void addCost(Integer id, ItemStack... cost) {
		CircuitUID uid = CircuitUID.fromInteger(id);
		persistentCosts.put(uid, Lists.newArrayList(cost));
	}
	
	
	/**
	 * Gets the "raw" recipe for a circuit -- that is, the recipe as if we set the config options for
	 * a linear scale with multiplier 1 (recursive sum of all circuit component costs at the time of serialization)
	 * @param worldIn
	 * @param playerID
	 * @param uid
	 * @return
	 */
	public static Optional<List<ItemStack>> getRawRecipeFor(World worldIn, UUID playerID, CircuitUID uid) {
		//TODO: Should the persistent costs be affected by any of this?
		
		if (persistentCosts.containsKey(uid)) {
			return Optional.of(SerializableItemStack.copyOf(persistentCosts.get(uid)));
		}
		
		try {
			Optional<EntityPlayer> player = EntityUtils.getPlayerFromUID(worldIn, playerID);
			if (!Config.isCircuitsProgressWorldGlobal && !player.isPresent()) {
				Log.internalError("Could not find player with UUID " + playerID);
				return Optional.empty();
			}
			File recipeFile = getRecipeFileFor(player.orElse(null), uid);
			if (!recipeFile.exists()) {
				return Optional.empty();
			}
			List<ItemStack> itemStacks = SerializableItemStack.itemStacksFromFile(recipeFile);
			return Optional.of(itemStacks);

		}
		catch (IOException e) {
			Log.internalError("Exception when reading recipe " + e);
			
			return Optional.empty();
		}
	}

	public static Item itemFromState(IBlockState in) {
		return Optional.ofNullable(Item.getItemFromBlock(in.getBlock()))
	             .orElseGet(() -> in.getBlock().getItemDropped(in, ThreadLocalRandom.current(), 0));
	}
	
	public static Optional<ItemStack> unitItemStackFromState(IBlockState in) {
		Item item = itemFromState(in);
		if (item != null) {
			int meta = in.getBlock().getMetaFromState(in);
			return Optional.of(new ItemStack(item, 1, meta));
		}
		return Optional.empty();
	}

}
