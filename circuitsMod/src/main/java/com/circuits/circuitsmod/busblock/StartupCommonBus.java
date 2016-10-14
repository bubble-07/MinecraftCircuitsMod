package com.circuits.circuitsmod.busblock;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonBus
{
  public static BusBlock busBlock; 
  public static ItemBlock itembusBlock;

  public static void preInitCommon()
  {
    busBlock = (BusBlock)(new BusBlock.NarrowBusBlock().setUnlocalizedName("busblock"));
    busBlock.setRegistryName("busblock");
    GameRegistry.register(busBlock);

    itembusBlock = new ItemBlock(busBlock);
    itembusBlock.setRegistryName(busBlock.getRegistryName());
    GameRegistry.register(itembusBlock);
  }

  public static void initCommon()
  {
  }

  public static void postInitCommon()
  {
  }

}
