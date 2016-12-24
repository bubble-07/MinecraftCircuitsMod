package com.circuits.circuitsmod.busblock;

import com.circuits.circuitsmod.common.ItemBlockMeta;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
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
	  //2-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 8, 0), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(Items.REDSTONE));
      //4-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 8, 1), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(Items.GOLD_NUGGET));
      //8-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 8, 2), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()));
      //16-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 8, 3), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.STONE), 'y', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
      //32-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 8, 4), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.GLASS), 'y', new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
      //64-bit bus recipe
      GameRegistry.addRecipe(new ItemStack(busBlock, 16, 5), "xxx", "xyx", "xxx", 'x', new ItemStack(Blocks.GLASS), 'y', new ItemStack(Items.DIAMOND));


  }

  public static void postInitCommon()
  {
  }

}
