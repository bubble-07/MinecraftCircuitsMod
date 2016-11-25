package com.circuits.circuitsmod;

import com.circuits.circuitsmod.CommonProxy;
import com.circuits.circuitsmod.world.PuzzleTeleportCommand;
import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.network.ClientHandlers;
import com.circuits.circuitsmod.network.ServerHandlers;
import com.circuits.circuitsmod.network.TypedMessage;

import java.util.logging.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = CircuitsMod.MODID, version = CircuitsMod.VERSION)
public class CircuitsMod
{
    public static final String MODID = "circuitsmod";
    public static final String VERSION = "1.0";
    public static Logger logger;
    
    public static int dimensionId = -9; //placeholder
    
    public static SimpleNetworkWrapper network;
    
    @Mod.Instance(CircuitsMod.MODID)
    public static CircuitsMod instance;
    
    @SidedProxy(clientSide="com.circuits.circuitsmod.ClientOnlyProxy", serverSide="com.circuits.circuitsmod.DedicatedServerProxy")
    public static CommonProxy proxy;
    
	public static class ServerRequestHandler implements IMessageHandler<TypedMessage, IMessage> {
		@Override
		public IMessage onMessage(TypedMessage message, MessageContext ctx) {
			ServerHandlers.dispatch(message, ctx.getServerHandler().playerEntity.worldObj);
			return null;
		}
	}
	
	public static class ClientRequestHandler implements IMessageHandler<TypedMessage, IMessage> {
		@Override
		public IMessage onMessage(TypedMessage message, MessageContext ctx) {
			ClientHandlers.dispatch(message);
			return null;
		}
	}

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	network = NetworkRegistry.INSTANCE.newSimpleChannel("CircuitsChannel");
    	network.registerMessage(ServerRequestHandler.class, TypedMessage.class, 0, Side.SERVER);
    	network.registerMessage(ClientRequestHandler.class, TypedMessage.class, 1, Side.CLIENT);
    	
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

