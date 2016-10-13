package com.circuits.circuitsmod;

import net.minecraft.entity.player.EntityPlayer;

/**
 * CommonProxy is used to set up the mod and start it running.  It contains all the code that should run on both the
 *   Standalone client and the dedicated server.
 */
public abstract class CommonProxy {

  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
	  com.circuits.circuitsmod.frameblock.StartupCommon.preInitCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommon.preInitCommon();
  }
  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void init()
  {
	  com.circuits.circuitsmod.frameblock.StartupCommon.initCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommon.initCommon();
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {
	  com.circuits.circuitsmod.frameblock.StartupCommon.postInitCommon();
	  com.circuits.circuitsmod.controlblock.StartupCommon.postInitCommon();
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
