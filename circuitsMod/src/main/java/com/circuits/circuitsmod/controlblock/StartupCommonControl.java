package com.circuits.circuitsmod.controlblock;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.controlblock.gui.ControlGuiHandler;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;


public class StartupCommonControl
{
	public static Block controlBlock; 
	public static ItemBlock itemControlBlock; 

	public static void preInitCommon()
	{
		controlBlock = (ControlBlock)(new ControlBlock().setRegistryName("controlblock"));
		controlBlock.setUnlocalizedName("controlblock");
		GameRegistry.register(controlBlock);

		itemControlBlock = new ItemBlock(controlBlock);
		itemControlBlock.setRegistryName(controlBlock.getRegistryName());
		GameRegistry.register(itemControlBlock);

		GameRegistry.registerTileEntity(ControlTileEntity.class, "controlblock");

        NetworkRegistry.INSTANCE.registerGuiHandler(CircuitsMod.instance, new ControlGuiHandler());
	}

	public static void initCommon()
	{
	      GameRegistry.addRecipe(new ItemStack(controlBlock, 1), "xyx", "yzy", "xyx", 
	    		                 'x', new ItemStack(Blocks.OBSIDIAN), 
	    		                 'y', new ItemStack(Items.DIAMOND),
	    		                 'z', new ItemStack(Items.REDSTONE));

	}

	public static void postInitCommon()
	{
	}
}
