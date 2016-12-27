package com.circuits.circuitsmod.busblock;

import com.circuits.circuitsmod.common.ItemBlockMeta;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonBus
{
  public static BusBlock busBlock; 
  public static ItemBlock itembusBlock;

  public static void preInitCommon()
  {
    busBlock = (BusBlock)(new BusBlock().setUnlocalizedName("busblock"));
    busBlock.setRegistryName("busblock");
    GameRegistry.register(busBlock);
    
    itembusBlock = new ItemBlockMeta(busBlock);
    itembusBlock.setRegistryName(busBlock.getRegistryName());
    GameRegistry.register(itembusBlock);
  }

  public static void initCommon()
  {
	  //2-bit bus creation recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 1, 0), "xx", 'x', new ItemStack(Items.REDSTONE));
      
      //Other-bit bus creation recipes
      for (int i = 1; i < BusBlock.busWidths.length; i++) {
          GameRegistry.addRecipe(new ItemStack(busBlock, 2, i), "xxx", 'x', new ItemStack(busBlock, 1, i - 1));
      }
      
      //2-bit bus deconstruction recipe
      GameRegistry.addRecipe(new ItemStack(Items.REDSTONE, 2), "x", 'x', new ItemStack(busBlock, 1, 0));
      
      //Other-bit bus deconstruction recipes
      for (int i = 1; i < BusBlock.busWidths.length; i++) {
          GameRegistry.addRecipe(new ItemStack(busBlock, 3, i - 1), "xx", 'x', new ItemStack(busBlock, 1, i));
      }

  }

  public static void postInitCommon()
  {
  }

}
