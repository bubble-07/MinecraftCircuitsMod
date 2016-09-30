package minecraftbyexample.mbe50_particle;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

/**
 * User: The Grey Ghost
 * Date: 03/11/2015
 *
 * BlockFlameEmitter is a simple block made from a couple of smaller pieces.
 * See mbe02_block_partial for more information
 * The interesting part for Particle is randomDisplayTick(), which spawns our FlameParticle... see below.
 */
public class BlockFlameEmitter extends Block
{
  public BlockFlameEmitter()
  {
    super(Material.field_151576_e);
    this.func_149647_a(CreativeTabs.field_78031_c);   // the block will appear on the Decorations tab in creative
  }

  // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k()
  {
    return BlockRenderLayer.SOLID;
  }

  // used by the renderer to control lighting and visibility of other blocks.
  // set to false because this block doesn't fill the entire 1x1x1 space
  @Override
  public boolean func_149662_c(IBlockState state)
  {
    return false;
  }

  // used by the renderer to control lighting and visibility of other blocks, also by
  // (eg) wall or fence to control whether the fence joins itself to this block
  // set to false because this block doesn't fill the entire 1x1x1 space
  @Override
  public boolean func_149686_d(IBlockState state)
  {
    return false;
  }

  // render using a BakedModel (mbe30_inventory_basic.json --> mbe30_inventory_basic_model.json)
  // not required because the default (super method) is MODEL
  @Override
  public EnumBlockRenderType func_149645_b(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }

  // This method is called at random intervals - typically used by blocks which produce occasional effects, like
  //  smoke from a torch or stars from a portal.
  //  In this case, we use it to spawn two different types of Particle- vanilla, or custom.
  // Don't forget     @SideOnly(Side.CLIENT) otherwise this will crash on a dedicated server.
  @Override
  @SideOnly(Side.CLIENT)
  public void func_180655_c(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
  {
    // Particle must be spawned on the client only.
    // If you want the server to be able to spawn Particle, you need to send a network message to the client and get the
    //   client to spawn the Particle in response to the message (see mbe60 MessageHandlerOnClient for an example).
    if (worldIn.field_72995_K) {  // is this on the client side?
      // first example:
      // spawn a vanilla particle of LAVA type (smoke from lava)
      //  The starting position is the [x,y,z] of the tip of the pole (i.e. at [0.5, 1.0, 0.5] relative to the block position)
      //  Set the initial velocity to zero.
      // When the particle is spawned, it will automatically add a random amount of velocity - see EntityLavaFX constructor and
      //   Particle constructor.  This can be a nuisance if you don't want your Particle to have a random starting velocity!  See
      //  second example below for more information.

      double xpos = pos.func_177958_n() + 0.5;
      double ypos = pos.func_177956_o() + 1.0;
      double zpos = pos.func_177952_p() + 0.5;
      double velocityX = 0; // increase in x position every tick
      double velocityY = 0; // increase in y position every tick;
      double velocityZ = 0; // increase in z position every tick
      int [] extraInfo = new int[0];  // extra information if needed by the particle - in this case unused

      worldIn.func_175688_a(EnumParticleTypes.LAVA, xpos, ypos, zpos, velocityX, velocityY, velocityZ, extraInfo);

      // second example:
      // spawn a custom Particle ("FlameParticle") with a texture we have added ourselves.
      // FlameParticle also has custom movement and collision logic - it moves in a straight line until it hits something.
      // To make it more interesting, the stream of fireballs will target the nearest non-player entity within 16 blocks at
      //   the height of the pole or above.

      // starting position = top of the pole
      xpos = pos.func_177958_n() + 0.5;
      ypos = pos.func_177956_o() + 1.0;
      zpos = pos.func_177952_p() + 0.5;

      EntityMob mobTarget = getNearestTargetableMob(worldIn, xpos, ypos, zpos);
      Vec3d fireballDirection;
      if (mobTarget == null) { // no target: fire straight upwards
        fireballDirection = new Vec3d(0.0, 1.0, 0.0);
      } else {  // otherwise: aim at the mob
        // the direction that the fireball needs to travel is calculated from the starting point (the pole) and the
        //   end point (the mob's eyes).  A bit of googling on vector maths will show you that you calculate this by
        //  1) subtracting the start point from the end point
        //  2) normalising the vector (if you don't do this, then the fireball will fire faster if the mob is further away

        fireballDirection = mobTarget.func_174824_e(1.0F).func_178786_a(xpos, ypos, zpos);  // NB this method only works on client side
        fireballDirection = fireballDirection.func_72432_b();
      }

      // the velocity vector is now calculated as the fireball's speed multiplied by the direction vector.

      final double SPEED_IN_BLOCKS_PER_SECOND = 2.0;
      final double TICKS_PER_SECOND = 20;
      final double SPEED_IN_BLOCKS_PER_TICK = SPEED_IN_BLOCKS_PER_SECOND / TICKS_PER_SECOND;

      velocityX = SPEED_IN_BLOCKS_PER_TICK * fireballDirection.field_72450_a; // how much to increase the x position every tick
      velocityY = SPEED_IN_BLOCKS_PER_TICK * fireballDirection.field_72448_b; // how much to increase the y position every tick
      velocityZ = SPEED_IN_BLOCKS_PER_TICK * fireballDirection.field_72449_c; // how much to increase the z position every tick

      FlameParticle newEffect = new FlameParticle(worldIn, xpos, ypos, zpos, velocityX, velocityY, velocityZ);
      Minecraft.func_71410_x().field_71452_i.func_78873_a(newEffect);
    }
  }

  /**
   * Returns the nearest targetable mob to the indicated [xpos, ypos, zpos].
   * @param world
   * @param xpos [x,y,z] position to s
   * @param ypos
   * @param zpos
   * @return the nearest mob, or null if none within range.
   */
  private EntityMob getNearestTargetableMob(World world, double xpos, double ypos, double zpos) {
    final double TARGETING_DISTANCE = 16;
    AxisAlignedBB targetRange = new AxisAlignedBB(xpos - TARGETING_DISTANCE,
                                                  ypos,
                                                  zpos - TARGETING_DISTANCE,
                                                  xpos + TARGETING_DISTANCE,
                                                  ypos + TARGETING_DISTANCE,
                                                  zpos + TARGETING_DISTANCE);

    List<EntityMob> allNearbyMobs = world.func_72872_a(EntityMob.class, targetRange);
    EntityMob nearestMob = null;
    double closestDistance = Double.MAX_VALUE;
    for (EntityMob nextMob : allNearbyMobs) {
      double nextClosestDistance = nextMob.func_70092_e(xpos, ypos, zpos);
      if (nextClosestDistance < closestDistance) {
        closestDistance = nextClosestDistance;
        nearestMob = nextMob;
      }
    }
    return nearestMob;
  }

}
