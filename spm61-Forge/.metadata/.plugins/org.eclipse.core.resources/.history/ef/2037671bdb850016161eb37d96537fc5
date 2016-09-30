package minecraftcircuits.frameblock;

import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * User: The Grey Ghost
 * Date: 24/12/2014
 *
 * The Startup classes for this example are called during startup, in the following order:
 *  preInitCommon
 *  preInitClientOnly
 *  initCommon
 *  initClientOnly
 *  postInitCommon
 *  postInitClientOnly
 *  See MinecraftByExample class for more information
 */
public class StartupCommon
{
  public static FrameBlock frameBlock;  // this holds the unique instance of your block
  public static ItemBlock itemFrameBlock;  // this holds the unique instance of the ItemBlock corresponding to your block

  public static void preInitCommon()
  {
    // each instance of your block should have two names:
    // 1) a registry name that is used to uniquely identify this block.  Should be unique within your mod.  use lower case.
    // 2) an 'unlocalised name' that is used to retrieve the text name of your block in the player's language.  For example-
    //    the unlocalised name might be "water", which is printed on the user's screen as "Wasser" in German or
    //    "aqua" in Italian.
    //
    //    Multiple blocks can have the same unlocalised name - for example
    //  +----RegistryName----+---UnlocalisedName----+
    //  |  flowing_water     +       water          |
    //  |  stationary_water  +       water          |
    //  +--------------------+----------------------+
    //
    frameBlock = (FrameBlock)(new FrameBlock().setUnlocalizedName("frameblock_unlocalised_name"));
    frameBlock.setRegistryName("frameblock_registry_name");
    GameRegistry.register(frameBlock);

    // We also need to create and register an ItemBlock for this block otherwise it won't appear in the inventory
    itemFrameBlock = new ItemBlock(frameBlock);
    itemFrameBlock.setRegistryName(frameBlock.getRegistryName());
    GameRegistry.register(itemFrameBlock);
  }

  public static void initCommon()
  {
  }

  public static void postInitCommon()
  {
  }

}
