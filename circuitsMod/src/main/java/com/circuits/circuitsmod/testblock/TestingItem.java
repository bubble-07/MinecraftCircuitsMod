package com.circuits.circuitsmod.testblock;

import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.StringUtils;

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
	

	public static Optional<Integer> getLevelID(ItemStack stack) {
		String displayName = stack.getDisplayName();
		int secretIndex = displayName.indexOf(StringUtils.NULL_SYMBOL, 0);
		if (secretIndex == -1) {
			secretIndex = displayName.indexOf(StringUtils.SECTION_SYMBOL, 0);
			if (secretIndex == -1) {
				Log.internalError("Invalid Item Name Supplied.");
				return Optional.empty();
			}
		}
		String secretString = displayName.substring(secretIndex, displayName.length());
		int levelID = StringUtils.secretToInt(secretString);
		return Optional.of(Integer.valueOf(levelID));
	}
}
