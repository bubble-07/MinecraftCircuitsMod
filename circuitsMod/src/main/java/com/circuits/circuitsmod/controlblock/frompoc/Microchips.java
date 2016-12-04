package com.circuits.circuitsmod.controlblock.frompoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.gui.net.SpecializationValidationRequest;
import com.circuits.circuitsmod.controlblock.tester.net.CraftingRequest;
import com.circuits.circuitsmod.controlblock.tester.net.StringMessage;
import com.circuits.circuitsmod.controlblock.tester.net.TestRequest;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.controlblock.tester.net.TestStopRequest;
import com.circuits.circuitsmod.recipes.RecipeGraph;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(name = Microchips.MODNAME, modid = Microchips.MODID, version = Microchips.VERSION)
public class Microchips
{
    public static final String MODID = "microchips";
    public static final String VERSION = "1.0";
    public static final String MODNAME = "microchips";
    public static ControlBlock controlBlock = new ControlBlock();
    
    public static SimpleNetworkWrapper network;
    
    public static RecipeGraph recipeGraph = null;
    
    //TODO: Add server code to periodically reload the main model when __nobody__ is looking
    public static CircuitListModel mainModel = null;
    
    public static Map<String, ResourceLocation> texMap = new HashMap<>();
    
    //To be called from the server
    public static void ensureServerModelInit() {
    	if (mainModel == null) {
    		mainModel = new CircuitListModel();
    	}
    }
    
    //To be called from the client
    public static void requestClientModelUpdate() {
    	mainModel = null;	
    	network.sendToServer(new StringMessage("Send MainScreenModel"));
    }
    
	//For handling updates to this object from the server
	public static class MainModelHandler implements IMessageHandler<CircuitListModel, IMessage> {
		@Override
		public IMessage onMessage(CircuitListModel newModel, MessageContext ctxt) {
			System.out.println("Receiving model");
			
			mainModel = newModel;
			System.out.println("Number of model entries:" + mainModel.numEntries());
			return null;
		}
	}
	
	public static class ServerRequestHandler implements IMessageHandler<StringMessage, IMessage> {
		@Override
		public IMessage onMessage(StringMessage msg, MessageContext ctxt) {
			if (msg.text.equals("Send MainScreenModel")) {		
				ensureServerModelInit();
				network.sendToAll(mainModel);
				System.out.println("Number of model entries:" + mainModel.numEntries());
			}
			return null;
		}
	}
	
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
    
    
    @Instance("microchips")
    public static Microchips instance;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	network = NetworkRegistry.INSTANCE.newSimpleChannel("MicrochipChannel");
    	network.registerMessage(MainModelHandler.class, CircuitListModel.class, 0, Side.CLIENT);
    	network.registerMessage(ServerRequestHandler.class, StringMessage.class, 1, Side.SERVER);
    	network.registerMessage(ServerTestHandler.class, TestRequest.Message.class, 2, Side.SERVER);
    	network.registerMessage(TestStateUpdate.Message.Handler.class, TestStateUpdate.Message.class, 3, Side.CLIENT);
    	network.registerMessage(ServerCraftHandler.class, CraftingRequest.Message.class, 4, Side.SERVER);
    	network.registerMessage(ServerTestStopHandler.class, TestStopRequest.Message.class, 5, Side.SERVER);
    	network.registerMessage(SpecializationValidationHandler.class, SpecializationValidationRequest.Message.class, 6, Side.SERVER);

    }
    
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    	recipeGraph = new RecipeGraph();
    }
}
