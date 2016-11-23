package com.circuits.circuitsmod.busblock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;


public class StartupClientBus
{
	private static final ModelResourceLocation[] itemModelResourceLocations = {
		new ModelResourceLocation("circuitsmod:busblock_twobit", "inventory"),
		new ModelResourceLocation("circuitsmod:busblock_fourbit", "inventory"),
		new ModelResourceLocation("circuitsmod:busblock_eightbit", "inventory"),
		new ModelResourceLocation("circuitsmod:busblock_sixteenbit", "inventory"),
		new ModelResourceLocation("circuitsmod:busblock_thirtytwobit", "inventory"),
		new ModelResourceLocation("circuitsmod:busblock_sixtyfourbit", "inventory")
	};
	
	public static void preInitClientOnly()
	{    
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			ModelLoader.setCustomModelResourceLocation(StartupCommonBus.itembusBlock, i, itemModelResourceLocations[i]);
		}
		ResourceLocation[] rscs = new ResourceLocation[BusBlock.busWidths.length];
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			rscs[i] = itemModelResourceLocations[i];
			
		}
		ModelBakery.registerItemVariants(StartupCommonBus.itembusBlock, rscs);


	}

	public static void initClientOnly()
	{    
		for (int i = 0; i < BusBlock.busWidths.length; i++) {
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(StartupCommonBus.itembusBlock, i, itemModelResourceLocations[i]);
		}
	}

	public static void postInitClientOnly()
	{
	}
}
