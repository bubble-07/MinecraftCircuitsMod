package com.circuits.circuitsmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * ClientProxy is used to set up the mod and start it running on normal minecraft.  It contains all the code that should run on the
 *   client side only.
 *   For more background information see here http://greyminecraftcoder.blogspot.com/2013/11/how-forge-starts-up-your-code.html
 */
public class ClientOnlyProxy extends CommonProxy
{

  /**
   * Run before anything else. Read your config, create blocks, items, etc, and register them with the GameRegistry
   */
  public void preInit()
  {
    super.preInit();
    com.circuits.circuitsmod.frameblock.StartupClientFrame.preInitClientOnly();
    com.circuits.circuitsmod.controlblock.StartupClientControl.preInitClientOnly();
    com.circuits.circuitsmod.busblock.StartupClientBus.preInitClientOnly();
    com.circuits.circuitsmod.blockportalpuzzle.StartupClientPortal.preInitClientOnly();
    com.circuits.circuitsmod.telecleaner.StartupClientCleaner.preInitClientOnly();
    //com.circuits.circuitsmod.portalitem.StartupClientActivator.preInitClientOnly();
  }

  /**
   * Do your mod setup. Build whatever data structures you care about. Register recipes,
   * send FMLInterModComms messages to other mods.
   */
  public void init()
  {
    super.init();
    com.circuits.circuitsmod.frameblock.StartupClientFrame.initClientOnly();
    com.circuits.circuitsmod.controlblock.StartupClientControl.initClientOnly();
    com.circuits.circuitsmod.busblock.StartupClientBus.initClientOnly();
    com.circuits.circuitsmod.blockportalpuzzle.StartupClientPortal.initClientOnly();
    com.circuits.circuitsmod.telecleaner.StartupClientCleaner.initClientOnly();
    //com.circuits.circuitsmod.portalitem.StartupClientActivator.initClientOnly();
  }

  /**
   * Handle interaction with other mods, complete your setup based on this.
   */
  public void postInit()
  {
    super.postInit();
    com.circuits.circuitsmod.frameblock.StartupClientFrame.postInitClientOnly();
    com.circuits.circuitsmod.controlblock.StartupClientControl.postInitClientOnly();
    com.circuits.circuitsmod.busblock.StartupClientBus.postInitClientOnly();
    com.circuits.circuitsmod.blockportalpuzzle.StartupClientPortal.postInitClientOnly();
    com.circuits.circuitsmod.telecleaner.StartupClientCleaner.preInitClientOnly();
    com.circuits.circuitsmod.telecleaner.StartupClientCleaner.postInitClientOnly();
    //com.circuits.circuitsmod.portalitem.StartupClientActivator.postInitClientOnly();
  }

  @Override
  public boolean playerIsInCreativeMode(EntityPlayer player) {
    if (player instanceof EntityPlayerMP) {
      EntityPlayerMP entityPlayerMP = (EntityPlayerMP)player;
      return entityPlayerMP.interactionManager.isCreative();
    } else if (player instanceof EntityPlayerSP) {
      return Minecraft.getMinecraft().playerController.isInCreativeMode();
    }
    return false;
  }

  @Override
  public boolean isDedicatedServer() {return false;}

}
