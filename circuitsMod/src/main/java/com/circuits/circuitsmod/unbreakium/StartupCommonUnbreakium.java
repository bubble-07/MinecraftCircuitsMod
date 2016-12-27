package com.circuits.circuitsmod.unbreakium;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonUnbreakium
{
  public static UnbreakiumBlock unbreakiumBlock;
  public static ItemBlock itemUnbreakiumBlock;

  public static void preInitCommon()
  {
    unbreakiumBlock = (UnbreakiumBlock)(new UnbreakiumBlock().setUnlocalizedName("unbreakiumblock"));
    unbreakiumBlock.setRegistryName("unbreakiumblock");
    GameRegistry.register(unbreakiumBlock);

    itemUnbreakiumBlock = new ItemBlock(unbreakiumBlock);
    itemUnbreakiumBlock.setRegistryName(unbreakiumBlock.getRegistryName());
    GameRegistry.register(itemUnbreakiumBlock);
  }

  public static void initCommon()
  {
  }

  public static void postInitCommon()
  {
  }

}
