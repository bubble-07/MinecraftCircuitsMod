package com.circuits.circuitsmod;

import com.circuits.circuitsmod.CommonProxy;

import com.circuits.circuitsmod.CircuitsMod;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = CircuitsMod.MODID, version = CircuitsMod.VERSION)
public class CircuitsMod
{
    public static final String MODID = "circuitsmod";
    public static final String VERSION = "1.0";
    
    public static int dimensionId = -9; //placeholder
    
    @Mod.Instance(CircuitsMod.MODID)
    public static CircuitsMod instance;
    
    @SidedProxy(clientSide="com.circuits.circuitsmod.ClientOnlyProxy", serverSide="com.circuits.circuitsmod.DedicatedServerProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
      proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
      proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
      proxy.postInit();
    }
    
    public static String prependModID(String name) {return MODID + ":" + name;}
    
    /**
     *     @EventHandler
    public void init(FMLInitializationEvent event)
    {
       // ItemStack dirtStack = new ItemStack(Blocks.dirt);
        //GameRegistry.addRecipe(new ItemStack(Blocks.diamond_block), " x ", "x x", " x ", 'x', dirtStack);
        
        GameRegistry.registerTileEntity(ControlTileEntity.class, "controltileentity");
        GameRegistry.registerTileEntity(CircuitTileEntity.class, "circuittileentity");
		GameRegistry.registerBlock(circuitBlock, CircuitItem.class, circuitBlock.getName());
        //GameRegistry.registerItem(circuitItem, circuitItem.getName());
		
		CircuitRendererManager manager = new CircuitRendererManager();
		ModelResourceLocation fake = new ModelResourceLocation(Microchips.MODID, "fakecircuit");
		CircuitSmartModel renderer = new CircuitSmartModel(fake);
		manager.registerItemRenderer((CircuitItem) Item.getItemFromBlock(circuitBlock), renderer);
        
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new ControlGuiHandler());
        
        if (event.getSide() == Side.CLIENT) {
        	loadBlockModel(frameBlock, frameBlock.getName());
        	loadBlockModel(controlBlock, controlBlock.getName());
        	//loadBlockModel(circuitBlock, circuitBlock.getName());
        	ClientRegistry.bindTileEntitySpecialRenderer(CircuitTileEntity.class, new CircuitEntitySpecialRenderer());
        }
    }
     */
    
    
}
