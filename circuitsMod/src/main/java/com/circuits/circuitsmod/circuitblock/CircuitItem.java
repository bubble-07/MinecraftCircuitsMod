package com.circuits.circuitsmod.circuitblock;


import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.GuiConfigEntries.ChatColorEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CircuitItem extends ItemBlock {
	private final String name = "circuititem";
	
	private static final String circuitUIDTag = "circuitUID";
	
	@SideOnly(Side.CLIENT)
	public CircuitSmartModel renderer;
	
	public CircuitItem(Block block) { 
		super(block);
		setUnlocalizedName(CircuitsMod.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setRenderer(CircuitSmartModel itemRenderer) {
		this.renderer = itemRenderer;
	}
	
	private static String intToSecret(int val) {
		String result = "";
		String[] chars = Integer.toString(val).split("");
		for (String c : chars) {
			result += "ø" + c;
		}
		return result;
	}
	private static int secretToInt(String str) {
		String toParse = "";
		String[] chars = str.split("");
		for (int i = 1; i < chars.length; i += 2) {
			toParse += chars[i];
		}
		return Integer.parseInt(toParse);
	}
	
	private static String getSecretString(SpecializedCircuitUID uid) {
		
		int val = uid.getUID().toInteger();
		String prefix = intToSecret(val);
		
		int[] optVals = uid.getOptions().asInts();
		if (optVals.length == 0) {
			return prefix;
		}
		
		String result = prefix;
		for (int i = 0; i < optVals.length; i++) {
			result += "ø ";
			result += intToSecret(optVals[i]);
		}
		return result;
	}
	
	private static SpecializedCircuitUID fromSecretString(String str) {
		String separator = "ø ";
		if (!str.contains(separator)) {
			separator = "ø ";
		}
		if (!str.contains(separator)) {
			return new SpecializedCircuitUID(CircuitUID.fromInteger(secretToInt(str)), new CircuitConfigOptions());
		}
		
		String[] splitString = str.split(separator);
		
		CircuitUID uid = CircuitUID.fromInteger(secretToInt(splitString[0]));
		
		int[] opts = new int[splitString.length - 1];
		
		for (int i = 1; i < splitString.length; i++) {
			opts[i - 1] = secretToInt(splitString[i]);
		}
		CircuitConfigOptions config = new CircuitConfigOptions(opts);
		return new SpecializedCircuitUID(uid, config);
	}
	
	/**
	 * Part of a hilariously terrible hack. We need to
	 * store data with items while still keeping them stackable,
	 * so we store the data in a hidden string (control sequences)
	 * in the name of the item.
	 * @param uid
	 * @return
	 */
	private static String getStackNameFromUID(SpecializedCircuitUID uid) {
		String name = CircuitInfoProvider.getDisplayName(uid);
		String secretString = getSecretString(uid);
		return name + secretString;
	}
	
	public static ItemStack getStackFromUID(SpecializedCircuitUID uid) {
		ItemStack result = new ItemStack(StartupCommonCircuitBlock.itemcircuitBlock);
		result.setStackDisplayName(getStackNameFromUID(uid));
		return result;
	}
	
	public static Optional<SpecializedCircuitUID> getUIDFromStack(ItemStack stack) {
		String displayName = stack.getDisplayName();
		int secretIndex = displayName.indexOf("ø", 0);
		if (secretIndex == -1) {
			secretIndex = displayName.indexOf("ø", 0);
			if (secretIndex == -1) {
				Log.internalError("Circuit TE Item Stack has bad metadata.");
				return Optional.empty();
			}
		}
		String secretString = displayName.substring(secretIndex, displayName.length());
		return Optional.of(fromSecretString(secretString));
	}
}
