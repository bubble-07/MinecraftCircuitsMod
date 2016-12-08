package com.circuits.circuitsmod.common;

import net.minecraft.item.ItemStack;

/**
 * Yet another example of why there should be an apache-commons style library
 * for minecraft. Pulled from here:
 * http://bedrockminer.jimdo.com/modding-tutorials/basic-modding-1-8/blockstates-and-metadata/
 */
public interface IMetaBlockName {
	String getSpecialName(ItemStack stack);
}
