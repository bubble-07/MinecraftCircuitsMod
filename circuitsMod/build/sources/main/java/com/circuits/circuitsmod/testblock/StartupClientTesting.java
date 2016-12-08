
package com.circuits.circuitsmod.testblock;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * The Startup class for this example is called during startup, in the following order:
 *  preInitCommon
 *  preInitClientOnly
 *  initCommon
 *  initClientOnly
 *  postInitCommon
 *  postInitClientOnly
 *  See MinecraftByExample class for more information
 */
public class StartupClientTesting
{
	public static void preInitClientOnly()
	{
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:testingblock", "inventory");
		final int DEFAULT_ITEM_SUBTYPE = 0;
		ModelLoader.setCustomModelResourceLocation(StartupCommonTesting.itemTestingBlock, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
	}

	public static void initClientOnly()
	{

	}

	public static void postInitClientOnly()
	{
	}
}
