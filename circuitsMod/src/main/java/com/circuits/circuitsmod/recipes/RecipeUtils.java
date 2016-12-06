package com.circuits.circuitsmod.recipes;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.circuits.circuitsmod.Config;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.EntityUtils;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerializableItemStack;

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
		if (Config.isCircuitsProgressWorldGlobal) {
			File worldSavesDir = FileUtils.getWorldSaveDir();
			SerializableItemStack.itemStacksToFile(cost, new File(worldSavesDir + "/" + uid.toInteger() + "materials.nbt"));
		}
		else {
			NBTTagCompound playerCpd = player.getEntityData();
			NBTTagCompound costsCpd = playerCpd.getCompoundTag("CircuitCosts");
			NBTTagCompound recipeCpd = costsCpd.getCompoundTag(uid.toInteger() + "");
			NBTTagCompound itemStacks = SerializableItemStack.itemStacksToNBT(cost);
			recipeCpd.setTag("recipe", itemStacks);
		}
	}
	
	public static Optional<List<ItemStack>> getRecipeFor(World worldIn, UUID playerID, CircuitUID uid) {
		try {
			if (Config.isCircuitsProgressWorldGlobal) {
				File worldSavesDir = FileUtils.getWorldSaveDir();
				File recipeFile = new File(worldSavesDir + "/" + uid.toInteger() + "materials.nbt");
				if (!recipeFile.exists()) {
					return Optional.empty();
				}
				return Optional.of(SerializableItemStack.itemStacksFromFile(recipeFile));
			}
			else {
				Optional<EntityPlayer> player = EntityUtils.getPlayerFromUID(worldIn, playerID);
				if (!player.isPresent()) {
					Log.internalError("Could not find player with UUID " + playerID);
				}
				//TODO: Sync this with the client, so it knows what to display!
				NBTTagCompound playerCpd = player.get().getEntityData();
				NBTTagCompound costsCpd = playerCpd.getCompoundTag("CircuitCosts");
				NBTTagCompound recipeCpd = costsCpd.getCompoundTag(uid.toInteger() + "");
				NBTTagCompound recipeTag = recipeCpd.getCompoundTag("recipe");
				List<ItemStack> itemStacks = SerializableItemStack.itemStacksFromNBT(recipeTag);
				return Optional.of(itemStacks);
			}
		}
		catch (IOException e) {
			Log.internalError("Exception when reading recipe " + e);
			
			return Optional.empty();
		}
	}

}
