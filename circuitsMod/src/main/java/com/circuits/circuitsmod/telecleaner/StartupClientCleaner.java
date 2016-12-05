package com.circuits.circuitsmod.telecleaner;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

/**
 * The Startup classes for this example are called during startup, in the following order:
 *  preInitCommon
 *  preInitClientOnly
 *  initCommon
 *  initClientOnly 
 *  postInitCommon
 *  postInitClientOnly
 */
public class StartupClientCleaner
{
  public static void preInitClientOnly()
  {
    // This step is necessary in order to make your block render properly when it is an item (i.e. in the inventory
    //   or in your hand or thrown on the ground).
    // It must be done on client only, and must be done after the block has been created in Common.preinit().
    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:telecleaner", "inventory");
    final int DEFAULT_ITEM_SUBTYPE = 0;
    ModelLoader.setCustomModelResourceLocation(StartupCommonCleaner.itemTeleCleaner, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
  }

  public static void initClientOnly()
  {
  }

  public static void postInitClientOnly()
  {
  }
}
