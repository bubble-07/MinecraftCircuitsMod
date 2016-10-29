package com.circuits.circuitsmod.portalitem;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonActivator {

	public static ItemPuzzlePortalActivator itemActivator;  // this holds the unique instance of your block

	  public static void preInitCommon()
	  {
	    itemActivator = (ItemPuzzlePortalActivator)(new ItemPuzzlePortalActivator().setUnlocalizedName("portal_activator"));
	    itemActivator.setRegistryName("portal_activator");
	    GameRegistry.register(itemActivator);
	  }

	  public static void initCommon()
	  {
	  }

	  public static void postInitCommon()
	  {
	  }
}
