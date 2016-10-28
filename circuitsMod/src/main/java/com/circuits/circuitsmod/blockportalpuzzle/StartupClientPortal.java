package com.circuits.circuitsmod.blockportalpuzzle;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class StartupClientPortal {

	public static void preInitClientOnly() {
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:portalblock", "inventory");
		final int DEFAULT_ITEM_SUBTYPE = 0;
		ModelLoader.setCustomModelResourceLocation(StartupCommonPortal.itemblockPortalPuzzle, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
	}
	
	public static void initClientOnly()
	  {
	  }

	  public static void postInitClientOnly()
	  {
	  }
}
