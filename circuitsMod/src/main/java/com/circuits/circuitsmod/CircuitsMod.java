package com.circuits.circuitsmod;

import com.circuits.circuitsmod.CommonProxy;
import com.circuits.circuitsmod.world.PuzzleTeleportCommand;

import java.util.logging.Logger;

import com.circuits.circuitsmod.CircuitsMod;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.DimensionManager;

@Mod(modid = CircuitsMod.MODID, version = CircuitsMod.VERSION)
public class CircuitsMod
{
    public static final String MODID = "circuitsmod";
    public static final String VERSION = "1.0";
    public static Logger logger;
    
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
    
    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new PuzzleTeleportCommand());
    }
}

