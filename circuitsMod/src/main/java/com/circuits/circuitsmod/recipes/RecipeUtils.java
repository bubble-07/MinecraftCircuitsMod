package com.circuits.circuitsmod.recipes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
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
	public static void writeRecipeOut(EntityPlayer player, CircuitUID uid, List<ItemStack> cost) throws IOException {
		File recipeFile = getRecipeFileFor(player, uid);
		SerializableItemStack.itemStacksToFile(cost, recipeFile);
	}
	
	private static File getRecipeFileFor(EntityPlayer player, CircuitUID uid) throws IOException {
		File worldSavesDir = FileUtils.getWorldSaveDir();
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
	//or for things like inputs/outputs/combiners/splitters which are impossible to build (primitives)
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
		addCost(PersistentCircuitUIDs.DELAY_CIRCUIT, new ItemStack(Items.REPEATER, 2));
	}
	
	private static void addCost(Integer id, ItemStack... cost) {
		CircuitUID uid = CircuitUID.fromInteger(id);
		persistentCosts.put(uid, Lists.newArrayList(cost));
	}
	
	
	public static Optional<List<ItemStack>> getRecipeFor(World worldIn, UUID playerID, CircuitUID uid) {
		
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

}
