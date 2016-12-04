package com.circuits.circuitsmod.testblock;

import com.circuits.circuitsmod.GuiHandlerRegistry;
import com.circuits.circuitsmod.CircuitsMod;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * The Startup classes for this example are called during startup, in the following order:
 *  preInitCommon
 *  preInitClientOnly
 *  initCommon
 *  initClientOnly
 *  postInitCommon
 *  postInitClientOnly
 *  See MinecraftByExample class for more information
 */
public class StartupCommonTesting
{
	public static Block testingBlock;  // this holds the unique instance of your block
	public static ItemBlock itemTestingBlock; // and the corresponding item form that block

	public static void preInitCommon()
	{
		// each instance of your block should have a name that is unique within your mod.  use lower case.
		/* it is a good practise to use a consistent registry name and obtain the unlocalised name from the registry name,
		 *  this will avoid breaking old worlds if something changes. This would look like
		 *  		TestingBlock.getRegistryName().toString();
		 *  and would require changing the lang file as the block's name would be now
		 *          tile.minecraftbyexample:mbe_30_inventory_basic.name
		 */
		testingBlock = (TestingBlock)(new TestingBlock().setRegistryName("testingblock"));
		testingBlock.setUnlocalizedName("testingblock");
		GameRegistry.register(testingBlock);

		// same but for the associated item
		itemTestingBlock = new TestingItem(testingBlock);
		itemTestingBlock.setRegistryName(testingBlock.getRegistryName());
		GameRegistry.register(itemTestingBlock);

		// register the tile entity associated with the inventory block
		GameRegistry.registerTileEntity(TileEntityTesting.class, "testingblock");
	}

	public static void initCommon()
	{
	}

	public static void postInitCommon()
	{
	}
}
