package com.circuits.circuitsmod.controlblock;

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
public class StartupCommon
{
	public static Block controlBlock;  // this holds the unique instance of your block
	public static ItemBlock itemControlBlock; // and the corresponding item form that block

	public static void preInitCommon()
	{
		// each instance of your block should have a name that is unique within your mod.  use lower case.
		/* it is a good practise to use a consistent registry name and obtain the unlocalised name from the registry name,
		 *  this will avoid breaking old worlds if something changes. This would look like
		 *  		ControlBlock.getRegistryName().toString();
		 *  and would require changing the lang file as the block's name would be now
		 *          tile.minecraftbyexample:mbe_30_inventory_basic.name
		 */
		controlBlock = (ControlBlock)(new ControlBlock().setRegistryName("controlblock"));
		controlBlock.setUnlocalizedName("controlblock");
		GameRegistry.register(controlBlock);

		// same but for the associated item
		itemControlBlock = new ItemBlock(controlBlock);
		itemControlBlock.setRegistryName(controlBlock.getRegistryName());
		GameRegistry.register(itemControlBlock);

		// register the tile entity associated with the inventory block
		GameRegistry.registerTileEntity(TileEntityControl.class, "controlblock");

		// You need to register a GUIHandler for the container.  However there can be only one handler per mod, so for the purposes
		//   of this project, we create a single GuiHandlerRegistry for all examples.
		// We register this GuiHandlerRegistry with the NetworkRegistry, and then tell the GuiHandlerRegistry about
		//   each example's GuiHandler, in this case GuiHandlerMBE30, so that when it gets a request from NetworkRegistry,
		//   it passes the request on to the correct example's GuiHandler.
		NetworkRegistry.INSTANCE.registerGuiHandler(CircuitsMod.instance, GuiHandlerRegistry.getInstance());
		GuiHandlerRegistry.getInstance().registerGuiHandler(new GuiHandlerControl(), GuiHandlerControl.getGuiID());
	}

	public static void initCommon()
	{
	}

	public static void postInitCommon()
	{
	}
}
