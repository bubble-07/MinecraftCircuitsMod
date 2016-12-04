package com.circuits.circuitsmod.telecleaner;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * The Startup classes for this example are called during startup, in the following order:
 *  preInitCommon
 *  preInitClientOnly
 *  initCommon
 *  initClientOnly
 *  postInitCommon
 *  postInitClientOnly
 */
public class StartupCommonCleaner
{
  public static TeleCleaner teleCleaner;  // this holds the unique instance of your block
  public static ItemBlock itemTeleCleaner;  // this holds the unique instance of the ItemBlock corresponding to your block

  public static void preInitCommon()
  {
    teleCleaner = (TeleCleaner)(new TeleCleaner().setUnlocalizedName("TeleCleaner"));
    teleCleaner.setRegistryName("telecleaner");
    GameRegistry.register(teleCleaner);

    // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
    itemTeleCleaner = new ItemBlock(teleCleaner);
    itemTeleCleaner.setRegistryName(teleCleaner.getRegistryName());
    GameRegistry.register(itemTeleCleaner);
  }

  public static void initCommon()
  {
  }

  public static void postInitCommon()
  {
  }

}
