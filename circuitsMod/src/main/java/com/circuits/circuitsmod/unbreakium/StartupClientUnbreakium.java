package com.circuits.circuitsmod.unbreakium;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class StartupClientUnbreakium
{
  public static void preInitClientOnly()
  {
    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:unbreakiumblock", "inventory");
    final int DEFAULT_ITEM_SUBTYPE = 0;
    ModelLoader.setCustomModelResourceLocation(StartupCommonUnbreakium.itemUnbreakiumBlock, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
  }

  public static void initClientOnly()
  {
  }

  public static void postInitClientOnly()
  {
  }
}
