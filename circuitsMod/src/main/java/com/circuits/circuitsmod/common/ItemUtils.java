package com.circuits.circuitsmod.common;

import java.util.List;
import java.util.function.Function;

import net.minecraft.item.ItemStack;

public class ItemUtils {
	public static List<ItemStack> mapOverQty(List<ItemStack> stacks, Function<Integer, Integer> mapper) {
		for (ItemStack item : stacks) {
			item.stackSize = mapper.apply(item.stackSize);
		}
		return stacks;
	}
	public static List<ItemStack> sortQty(List<ItemStack> stacks) {
		stacks.sort((s1, s2) -> s1.stackSize - s2.stackSize);
		return stacks;
	}
	/**
	 * From an item stack that may take on the ore dictionary's wildcard value for its meta,
	 * ensure that the ItemStack may be rendered.
	 * @return
	 */
	public static ItemStack getRenderableItemStack(ItemStack in) {
		if (in.getMetadata() != Short.MAX_VALUE) {
			return in;
		}
		return new ItemStack(in.getItem(), in.stackSize, 0);
	}
}
