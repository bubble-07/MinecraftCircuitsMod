package com.circuits.circuitsmod.circuitblock;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class StartupClientCircuitBlock
{
  static CircuitRendererManager rendererManager;
  public static void preInitClientOnly()
  {
    final int DEFAULT_ITEM_SUBTYPE = 0;
    rendererManager = new CircuitRendererManager();
    ModelLoader.setCustomModelResourceLocation(StartupCommonCircuitBlock.itemcircuitBlock, DEFAULT_ITEM_SUBTYPE, CircuitSmartModel.variantTag);
  }

  public static void initClientOnly()
  {
	  ClientRegistry.bindTileEntitySpecialRenderer(CircuitTileEntity.class, new CircuitEntitySpecialRenderer());
  }

  public static void postInitClientOnly()
  {
  }
}
