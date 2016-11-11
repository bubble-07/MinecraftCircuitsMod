package com.circuits.circuitsmod.circuitblock;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.ModelLoader;

public class StartupClientCircuitBlock
{
  public static void preInitClientOnly()
  {
    ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:frameblock", "inventory");
    final int DEFAULT_ITEM_SUBTYPE = 0;
    ModelLoader.setCustomModelResourceLocation(StartupCommonCircuitBlock.itemcircuitBlock, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
  }

  public static void initClientOnly()
  {
  }

  public static void postInitClientOnly()
  {
  }
}
