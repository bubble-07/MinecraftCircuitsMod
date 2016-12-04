package com.circuits.circuitsmod.testblock;

import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.Log;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class TestingItem extends ItemBlock {
	
	private final String name = "testingitem";
	private static final String levelIDTag = "levelID";
	
	public TestingItem(Block block) {
		super(block);
		setUnlocalizedName(CircuitsMod.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
	
	private static String getSecretString(int val) {
		String result = "";
		String[] chars = Integer.toString(val).split("");
		for (String c : chars) {
			result += "ø" + c;
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
	

	public static Optional<Integer> getLevelID(ItemStack stack) {
		String displayName = stack.getDisplayName();
		int secretIndex = displayName.indexOf("ø", 0);
		if (secretIndex == -1) {
			Log.internalError("Invalid Item Name Supplied.");
			return Optional.empty();
		}
		String secretString = displayName.substring(secretIndex, displayName.length());
		int levelID = fromSecretString(secretString);
		return Optional.of(Integer.valueOf(levelID));
	}
	
	private static String getStackFromValue(int value) {
		String secretString = getSecretString(value);
		return secretString;
	}
	
	
	
}
