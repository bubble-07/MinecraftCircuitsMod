package com.circuits.circuitsmod.controlblock;

import com.circuits.circuitsmod.GuiHandlerRegistry;
import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.controlblock.gui.ControlGuiHandler;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;


public class StartupCommonControl
{
	public static Block controlBlock;  // this holds the unique instance of your block
	public static ItemBlock itemControlBlock; // and the corresponding item form that block

	public static void preInitCommon()
	{
		controlBlock = (ControlBlock)(new ControlBlock().setRegistryName("controlblock"));
		controlBlock.setUnlocalizedName("controlblock");
		GameRegistry.register(controlBlock);

		// same but for the associated item
		itemControlBlock = new ItemBlock(controlBlock);
		itemControlBlock.setRegistryName(controlBlock.getRegistryName());
		GameRegistry.register(itemControlBlock);

		// register the tile entity associated with the inventory block
		GameRegistry.registerTileEntity(ControlTileEntity.class, "controlblock");

        NetworkRegistry.INSTANCE.registerGuiHandler(CircuitsMod.instance, new ControlGuiHandler());
	}

	public static void initCommon()
	{
	}

	public static void postInitCommon()
	{
	}
}
