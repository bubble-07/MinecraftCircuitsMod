package com.circuits.circuitsmod;

import java.io.File;

import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.controlblock.gui.ControlGuiHandler;
import com.circuits.circuitsmod.world.PuzzleDimensions;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * CommonProxy is used to set up the mod and start it running.  It contains all the code that should run on both the
 *   Standalone client and the dedicated server.
 */
public abstract class CommonProxy {

  public static Configuration config;

/**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
	  File configRoot = FileUtils.getConfigRootDir();
	  configRoot.mkdirs();
	  File configFile = new File(configRoot + "/circuits.cfg");
	  
	  config = new Configuration(configFile);
	  
	  com.circuits.circuitsmod.frameblock.StartupCommonFrame.preInitCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommonControl.preInitCommon();
	  com.circuits.circuitsmod.busblock.StartupCommonBus.preInitCommon();
	  com.circuits.circuitsmod.blockportalpuzzle.StartupCommonPortal.preInitCommon();
	  com.circuits.circuitsmod.telecleaner.StartupCommonCleaner.preInitCommon();
	  com.circuits.circuitsmod.testblock.StartupCommonTesting.preInitCommon();
	  //com.circuits.circuitsmod.portalitem.StartupCommonActivator.preInitCommon();
  	  com.circuits.circuitsmod.circuitblock.StartupCommonCircuitBlock.preInitCommon();
  	  com.circuits.circuitsmod.unbreakium.StartupCommonUnbreakium.preInitCommon();
  	  Config.readConfig();
  	  
	  NetworkRegistry.INSTANCE.registerGuiHandler(CircuitsMod.instance, 
		      new ControlGuiHandler());  
  }
  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void init()
  {
	  com.circuits.circuitsmod.frameblock.StartupCommonFrame.initCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommonControl.initCommon();
	  com.circuits.circuitsmod.busblock.StartupCommonBus.initCommon();
	  com.circuits.circuitsmod.blockportalpuzzle.StartupCommonPortal.initCommon();
	  com.circuits.circuitsmod.telecleaner.StartupCommonCleaner.initCommon();
	  com.circuits.circuitsmod.testblock.StartupCommonTesting.initCommon();
	  //com.circuits.circuitsmod.portalitem.StartupCommonActivator.initCommon();
	  com.circuits.circuitsmod.circuitblock.StartupCommonCircuitBlock.initCommon();
  	  com.circuits.circuitsmod.unbreakium.StartupCommonUnbreakium.initCommon();

	  
	  PuzzleDimensions.init();
	  
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {
	  com.circuits.circuitsmod.frameblock.StartupCommonFrame.postInitCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommonControl.postInitCommon();
	  com.circuits.circuitsmod.busblock.StartupCommonBus.postInitCommon();
	  com.circuits.circuitsmod.blockportalpuzzle.StartupCommonPortal.postInitCommon();
	  com.circuits.circuitsmod.telecleaner.StartupCommonCleaner.postInitCommon();
	  com.circuits.circuitsmod.testblock.StartupCommonTesting.postInitCommon();
	  com.circuits.circuitsmod.circuitblock.StartupCommonCircuitBlock.postInitCommon();
  	  com.circuits.circuitsmod.unbreakium.StartupCommonUnbreakium.postInitCommon();

	  
	  //com.circuits.circuitsmod.portalitem.StartupCommonActivator.postInitCommon();
  }

  // helper to determine whether the given player is in creative mode
  //  not necessary for most examples
  abstract public boolean playerIsInCreativeMode(EntityPlayer player);

  /**
   * is this a dedicated server?
   * @return true if this is a dedicated server, false otherwise
   */
  abstract public boolean isDedicatedServer();
  
}
