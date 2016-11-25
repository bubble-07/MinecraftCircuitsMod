package com.circuits.circuitsmod.circuitblock;


import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
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
	
	private static String getSecretString(int val) {
		String result = "";
		String[] chars = Integer.toString(val).split("");
		for (String c : chars) {
			result += "§" + c;
		}
		return result;
	}
	
	private static int fromSecretString(String str) {
		String toParse = "";
		String[] chars = str.split("");
		for (int i = 1; i < chars.length; i += 2) {
			toParse += chars[i];
		}
		return Integer.parseInt(toParse);
	}
	
	/**
	 * Part of a hilariously terrible hack. We need 
	 * @param uid
	 * @return
	 */
	private static String getStackNameFromUID(CircuitUID uid) {
		String name = CircuitInfoProvider.getDisplayName(uid);
		String secretString = getSecretString(uid.toInteger());
		return name + secretString;
	}
	
	public static ItemStack getStackFromUID(CircuitUID uid) {
		ItemStack result = new ItemStack(StartupCommonCircuitBlock.itemcircuitBlock);
		result.setStackDisplayName(getStackNameFromUID(uid));
		return result;
	}
	
	public static Optional<CircuitUID> getUIDFromStack(ItemStack stack) {
		String displayName = stack.getDisplayName();
		int secretIndex = displayName.indexOf("§", 0);
		if (secretIndex == -1) {
			secretIndex = displayName.indexOf("ø", 0);
			if (secretIndex == -1) {
				Log.internalError("Circuit TE Item Stack has bad metadata.");
				return Optional.empty();
			}
		}
		String secretString = displayName.substring(secretIndex, displayName.length());
		int uidIntValue = fromSecretString(secretString);
		return Optional.of(CircuitUID.fromInteger(uidIntValue));
	}
}
