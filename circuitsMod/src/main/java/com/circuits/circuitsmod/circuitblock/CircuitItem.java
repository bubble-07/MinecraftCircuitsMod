package com.circuits.circuitsmod.circuitblock;


import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
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
	
	private static String getSecretString(SpecializedCircuitUID uid) {
		
		int val = uid.getUID().toInteger();
		String prefix = StringUtils.intToSecret(val);
		
		int[] optVals = uid.getOptions().asInts();
		if (optVals.length == 0) {
			return prefix;
		}
		
		String result = prefix;
		for (int i = 0; i < optVals.length; i++) {
			result += StringUtils.SECTION_SYMBOL + " ";
			result += StringUtils.intToSecret(optVals[i]);
		}
		return result;
	}
	
	private static SpecializedCircuitUID fromSecretString(String str) {
		String separator = StringUtils.SECTION_SYMBOL + " ";
		if (!str.contains(separator)) {
			separator = StringUtils.NULL_SYMBOL + " ";
		}
		if (!str.contains(separator)) {
			return new SpecializedCircuitUID(CircuitUID.fromInteger(StringUtils.secretToInt(str)), new CircuitConfigOptions());
		}
		
		String[] splitString = str.split(separator);
		
		CircuitUID uid = CircuitUID.fromInteger(StringUtils.secretToInt(splitString[0]));
		
		int[] opts = new int[splitString.length - 1];
		
		for (int i = 1; i < splitString.length; i++) {
			opts[i - 1] = StringUtils.secretToInt(splitString[i]);
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
		int secretIndex = displayName.indexOf(StringUtils.SECTION_SYMBOL, 0);
		if (secretIndex == -1) {
			secretIndex = displayName.indexOf(StringUtils.NULL_SYMBOL, 0);
			if (secretIndex == -1) {
				Log.internalError("Circuit TE Item Stack has bad metadata.");
				return Optional.empty();
			}
		}
		String secretString = displayName.substring(secretIndex, displayName.length());
		return Optional.of(fromSecretString(secretString));
	}
}
