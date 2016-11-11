package com.circuits.circuitsmod.busblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;


public class StartupClientBus
{
	private static final ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("circuitsmod:busblock", "inventory");

	public static void preInitClientOnly()
	{    
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			ModelLoader.setCustomModelResourceLocation(StartupCommonBus.itembusBlock, i, itemModelResourceLocation);
		}
	}

	public static void initClientOnly()
	{    
		ResourceLocation[] rscs = new ResourceLocation[BusBlock.busWidths.length];
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			rscs[i] = itemModelResourceLocation;
			
		}
		ModelBakery.registerItemVariants(StartupCommonBus.itembusBlock, rscs);
		
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(StartupCommonBus.itembusBlock, i, itemModelResourceLocation);
		}

	}

	public static void postInitClientOnly()
	{
	}
}
