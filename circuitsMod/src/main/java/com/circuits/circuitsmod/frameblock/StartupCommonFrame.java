package com.circuits.circuitsmod.frameblock;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class StartupCommonFrame
{
  public static FrameBlock frameBlock;
  public static ItemBlock itemFrameBlock;

  public static void preInitCommon()
  {
    frameBlock = (FrameBlock)(new FrameBlock().setUnlocalizedName("frameblock"));
    frameBlock.setRegistryName("frameblock");
    GameRegistry.register(frameBlock);

    // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
    itemFrameBlock = new ItemBlock(frameBlock);
    itemFrameBlock.setRegistryName(frameBlock.getRegistryName());
    GameRegistry.register(itemFrameBlock);
  }

  public static void initCommon()
  {
	  GameRegistry.addRecipe(new ItemStack(frameBlock, 64), "yxx", "yyy", "xxy", 
              'x', new ItemStack(Blocks.OBSIDIAN), 
              'y', new ItemStack(Items.DYE, 1, EnumDyeColor.GREEN.getDyeDamage()));
  }

  public static void postInitCommon()
  {
  }

}
