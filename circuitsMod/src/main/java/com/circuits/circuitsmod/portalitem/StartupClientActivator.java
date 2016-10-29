package com.circuits.circuitsmod.portalitem;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class StartupClientActivator {
	public static void preInitClientOnly()
	  {
	    // required in order for the renderer to know how to render your item.
	    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:portalactivator", "inventory");
	    final int DEFAULT_ITEM_SUBTYPE = 0;
	    ModelLoader.setCustomModelResourceLocation(StartupCommonActivator.itemActivator, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
	  }

	  public static void initClientOnly()
	  {
	  }

	  public static void postInitClientOnly()
	  {
	  }
}
