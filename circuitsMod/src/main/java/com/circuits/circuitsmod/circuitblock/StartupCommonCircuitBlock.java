package com.circuits.circuitsmod.circuitblock;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonCircuitBlock
{
  public static CircuitBlock circuitBlock;
  public static CircuitItem itemcircuitBlock;

  public static void preInitCommon()
  {
    circuitBlock = (CircuitBlock)(new CircuitBlock().setUnlocalizedName("circuitblock"));
    circuitBlock.setRegistryName("circuitblock");
    GameRegistry.register(circuitBlock);
    
    GameRegistry.registerTileEntity(CircuitTileEntity.class, "circuittileentity");
    
    itemcircuitBlock = new CircuitItem(circuitBlock);
    itemcircuitBlock.setRegistryName(circuitBlock.getRegistryName());
    GameRegistry.register(itemcircuitBlock);
  }

  public static void initCommon()
  {
  }

  public static void postInitCommon()
  {
  }

}
