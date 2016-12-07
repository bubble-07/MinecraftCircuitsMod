package com.circuits.circuitsmod;

import com.circuits.circuitsmod.CommonProxy;
import com.circuits.circuitsmod.world.CircuitGiveCommand;
import com.circuits.circuitsmod.world.PuzzleTeleportCommand;
import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.controlblock.gui.net.CircuitCostRequest;
import com.circuits.circuitsmod.controlblock.gui.net.SpecializationValidationRequest;
import com.circuits.circuitsmod.controlblock.tester.net.CraftingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.network.ClientHandlers;
import com.circuits.circuitsmod.network.ServerHandlers;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.recipes.RecipeGraph;

import java.util.logging.Logger;

import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
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
    
    public static RecipeGraph recipeGraph = null;
    
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
	
    
	//TODO: Incorporate these request from the old mod format to the newer, slicker one
	
	public static class ServerTestHandler implements IMessageHandler<TestRequest.Message, IMessage> {
		@Override
		public IMessage onMessage(TestRequest.Message msg, MessageContext ctxt) {
			World world =  ctxt.getServerHandler().playerEntity.worldObj;
			TestRequest.handleTestRequest(msg.message, world);
			return null;
		}
	}
	
	public static class ServerTestStopHandler implements IMessageHandler<TestStopRequest.Message, IMessage> {
		@Override
		public IMessage onMessage(TestStopRequest.Message msg, MessageContext ctxt) {
			World world =  ctxt.getServerHandler().playerEntity.worldObj;
			TestStopRequest.handleTestStopRequest(msg.message, world);
			return null;
		}
	}
	
	public static class ServerCraftHandler implements IMessageHandler<CraftingRequest.Message, IMessage> {
		@Override
		public IMessage onMessage(CraftingRequest.Message msg, MessageContext ctxt) {
			World world =  ctxt.getServerHandler().playerEntity.worldObj;
			((IThreadListener) world).addScheduledTask(() -> {
				CraftingRequest.handleCraftingRequest(msg.message, world);
			});
			return null;
		}
	}
	
	public static class SpecializationValidationHandler implements IMessageHandler<SpecializationValidationRequest.Message, IMessage> {
		@Override
		public IMessage onMessage(SpecializationValidationRequest.Message msg, MessageContext ctxt) {
			World world =  ctxt.getServerHandler().playerEntity.worldObj;
			((IThreadListener) world).addScheduledTask(() -> {
				SpecializationValidationRequest.handleSpecializationValidationRequest(msg.message, world);
			});
			return null;
		}
	}
	
	public static class CircuitCostHandler implements IMessageHandler<CircuitCostRequest.Message, IMessage> {
		@Override
		public IMessage onMessage(CircuitCostRequest.Message msg, MessageContext ctxt) {
			World world =  ctxt.getServerHandler().playerEntity.worldObj;
			((IThreadListener) world).addScheduledTask(() -> {
				CircuitCostRequest.handleCircuitCostRequest(msg.message, world);
			});
			return null;
		}
	}

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	network = NetworkRegistry.INSTANCE.newSimpleChannel("CircuitsChannel");
    	network.registerMessage(ServerRequestHandler.class, TypedMessage.class, 0, Side.SERVER);
    	network.registerMessage(ClientRequestHandler.class, TypedMessage.class, 1, Side.CLIENT);
    	
    	network.registerMessage(ServerTestHandler.class, TestRequest.Message.class, 2, Side.SERVER);
    	network.registerMessage(TestStateUpdate.Message.Handler.class, TestStateUpdate.Message.class, 3, Side.CLIENT);
    	network.registerMessage(ServerCraftHandler.class, CraftingRequest.Message.class, 4, Side.SERVER);
    	network.registerMessage(ServerTestStopHandler.class, TestStopRequest.Message.class, 5, Side.SERVER);
    	network.registerMessage(SpecializationValidationHandler.class, SpecializationValidationRequest.Message.class, 6, Side.SERVER);
    	network.registerMessage(CircuitCostHandler.class, CircuitCostRequest.Message.class, 7, Side.SERVER);

    	
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
      recipeGraph = new RecipeGraph();
    }
    
    public static String prependModID(String name) {return MODID + ":" + name;}
    
    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new PuzzleTeleportCommand());
        event.registerServerCommand(new CircuitGiveCommand());

    }
}

