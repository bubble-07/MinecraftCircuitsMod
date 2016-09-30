package minecraftbyexample.mbe06_redstone.output_only;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * User: The Grey Ghost
 * Date: 24/11/2015
 *
 * BlockRedstoneTarget is designed to be hung on a wall.  When an arrow is fired into the target, it emits strong
 *   power into the wall.  The power level depends on which ring of the target the arrow is stuck in:
 *     15 for the bullseye, decreasing for every ring down to 0 for no arrow (or stuck in the wood)
 * For background information on blocks see here http://greyminecraftcoder.blogspot.com.au/2014/12/blocks-18.html
 */
public class BlockRedstoneTarget extends Block
{
  public BlockRedstoneTarget()
  {
    super(Material.field_151575_d);
    this.func_149647_a(CreativeTabs.field_78030_b);   // the block will appear on the Blocks tab in creative
  }

  //----- methods related to redstone

  /**
   * This block can provide power
   * @return
   */
  @Override
  public boolean func_149744_f(IBlockState iBlockState)
  {
    return true;
  }

  /** How much weak power does this block provide to the adjacent block?  In this example - none.
   * See http://greyminecraftcoder.blogspot.com.au/2015/11/redstone.html for more information
   * @param worldIn
   * @param pos the position of this block
   * @param state the blockstate of this block
   * @param side the side of the block - eg EAST means that this is to the EAST of the adjacent block.
   * @return The power provided [0 - 15]
   */
  @Override
  public int func_180656_a(IBlockState state, IBlockAccess worldIn, BlockPos pos,  EnumFacing side)
  {
    return 0;
  }

  /**
   *  The target provides strong power to the block it's mounted on (hanging on)
   * @param worldIn
   * @param pos the position of this block
   * @param state the blockstate of this block
   * @param side the side of the block - eg EAST means that this is to the EAST of the adjacent block.
   * @return The power provided [0 - 15]
   */

  @Override
  public int func_176211_b(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
  {
    EnumFacing targetFacing = (EnumFacing)state.func_177229_b(PROPERTYFACING);

    // only provide strong power through the back of the target.  If the target is facing east, that means
    //   it provides power to the block which lies to the west.
    // When this method is called by the adjacent block which lies to the west, the value of the side parameter is EAST.

    // The amount of power provided is related to how close the arrow hit to the bullseye.
    //  Bullseye = 15; Outermost ring = 3.

    if (side != targetFacing) return 0;
    if (!(worldIn instanceof World)) return 0;
    World world = (World)worldIn;  // We're provided with IBlockAccess instead of World because this is sometimes called
                                   //  during rendering, which is multithreaded and might be called using a ChunkCache.
                                   //  This might mean that the appearance of the adjacent block is sometimes not right,
                                   //   since we always return 0 for a ChunkCache.  I haven't managed to trigger this
                                   //  potential bug, but I can't rule it out.
    int bestRing = findBestArrowRing(world, pos, state);
    if (bestRing < 0) return 0;

    return 15 - 2 * bestRing;
  }

  /**
   * Called with an entity collides with the block.
   * In this case - we check if an arrow has collided.
   * @param worldIn
   * @param pos
   * @param state
   * @param entityIn
   */
  @Override
  public void func_180634_a(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
  {
    EnumFacing targetFacing = (EnumFacing)state.func_177229_b(PROPERTYFACING);

    if (!worldIn.field_72995_K) {
      if (entityIn instanceof EntityArrow) {
        AxisAlignedBB targetAABB = func_180646_a(state, worldIn, pos);
        AxisAlignedBB targetAABBinWorld = targetAABB.func_186670_a(pos);
        List<EntityArrow> embeddedArrows = worldIn.func_72872_a(EntityArrow.class, targetAABBinWorld);

        // when a new arrow hits, remove all others which are already embedded

        for (EntityArrow embeddedEntity : embeddedArrows) {
          if (embeddedEntity.func_145782_y() != entityIn.func_145782_y()) {
            embeddedEntity.func_70106_y();
          }
        }

        // notify my immediate neighbours, and also the immediate neighbours of the block I'm mounted on, because I
        //  am giving strong power to it.
        worldIn.func_175685_c(pos, this);
        EnumFacing directionOfNeighbouringWall = targetFacing.func_176734_d();
        worldIn.func_175685_c(pos.func_177972_a(directionOfNeighbouringWall), this);
      }
    }
  }

  /**
   * Perform a scheduled update for this block
   * @param worldIn
   * @param pos
   * @param state
   * @param rand
   */
  @Override
  public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand)
  {
    // depending on what your block does, you may need to implement updateTick and schedule updateTicks using
    //         worldIn.scheduleUpdate(pos, this, 4);
    // For vanilla examples see BlockButton, BlockRedstoneLight
    // nothing required for this example
  }

  /** For all the arrows stuck in the target, find the one which is the closest to the centre.
   *
   * @param worldIn
   * @param pos
   * @param state
   * @return the closest distance to the centre (eg 0->1 = centremost ring , 6 = outermost ring); or <0 for none.
   */
  private int findBestArrowRing(World worldIn, BlockPos pos, IBlockState state)
  {
    final int MISS_VALUE = -1;
    EnumFacing targetFacing = (EnumFacing)state.func_177229_b(PROPERTYFACING);
    AxisAlignedBB targetAABB = func_180646_a(state, worldIn, pos);
    AxisAlignedBB targetAABBinWorld = targetAABB.func_186670_a(pos);
    List<EntityArrow> embeddedArrows = worldIn.func_72872_a(EntityArrow.class, targetAABBinWorld);
    if (embeddedArrows.isEmpty()) return MISS_VALUE;

    double closestDistance = Float.MAX_VALUE;
    for (EntityArrow entity : embeddedArrows) {
      if (!entity.field_70128_L && entity instanceof EntityArrow) {
        EntityArrow entityArrow = (EntityArrow) entity;
        Vec3d hitLocation = getArrowIntersectionWithTarget(entityArrow, targetAABBinWorld);
        if (hitLocation != null) {
          Vec3d targetCentre = new Vec3d((targetAABBinWorld.field_72340_a + targetAABBinWorld.field_72336_d) / 2.0,
                                              (targetAABBinWorld.field_72338_b + targetAABBinWorld.field_72337_e) / 2.0,
                                              (targetAABBinWorld.field_72339_c + targetAABBinWorld.field_72334_f) / 2.0
          );
          Vec3d hitRelativeToCentre = hitLocation.func_178788_d(targetCentre);

          // Which ring did it hit?  Calculate it as the biggest deviation of y and (x and z) from the centre.

          double xDeviationPixels = 0;
          double yDeviationPixels = Math.abs(hitRelativeToCentre.field_72448_b * 16.0);
          double zDeviationPixels = 0;

          if (targetFacing == EnumFacing.EAST || targetFacing == EnumFacing.WEST) {
            zDeviationPixels = Math.abs(hitRelativeToCentre.field_72449_c * 16.0);
          } else {
            xDeviationPixels = Math.abs(hitRelativeToCentre.field_72450_a * 16.0);
          }

          double maxDeviationPixels = Math.max(yDeviationPixels, Math.max(xDeviationPixels, zDeviationPixels));
          if (maxDeviationPixels < closestDistance) {
            closestDistance = maxDeviationPixels;
          }

        }
      }
    }

    if (closestDistance == Float.MAX_VALUE) return MISS_VALUE;
    final int OUTERMOST_RING = 6;
    int ringHit = MathHelper.func_76128_c(closestDistance);
    return (ringHit <= OUTERMOST_RING) ? ringHit : MISS_VALUE;
  }

  /**
   * Find the point [x,y,z] that corresponds to where the arrow has struck the face of the target
   * @param arrow
   * @param targetAABB
   * @return
   */
  private static Vec3d getArrowIntersectionWithTarget(EntityArrow arrow, AxisAlignedBB targetAABB)
  {
    // create a vector that points in the same direction as the arrow.
    // Start with a vector pointing south - this corresponds to 0 degrees yaw and 0 degrees pitch
    // Then rotate about the x-axis to pitch up or down, then rotate about the y axis to yaw
    Vec3d arrowDirection = new Vec3d(0.0, 0.0, 10.0);
    float rotationPitchRadians = (float)Math.toRadians(arrow.field_70125_A);
    float rotationYawRadians = (float)Math.toRadians(arrow.field_70177_z);

    arrowDirection = arrowDirection.func_178789_a(-rotationPitchRadians);
    arrowDirection = arrowDirection.func_178785_b(+rotationYawRadians);

    Vec3d arrowRayOrigin = arrow.func_174791_d();
    Vec3d arrowRayEndpoint = arrowRayOrigin.func_178787_e(arrowDirection);
    RayTraceResult hitLocation = targetAABB.func_72327_a(arrowRayOrigin, arrowRayEndpoint);
    if (hitLocation == null) return null;
    if (hitLocation.field_72313_a != RayTraceResult.Type.BLOCK) return null;
    return hitLocation.field_72307_f;
  }

  // ---- methods to control placement of the target (must be on a solid wall)

  // When a neighbour changes - check if the supporting wall has been demolished
  @Override
  public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock)
  {
    if (!worldIn.field_72995_K) { // server side only
      EnumFacing enumfacing = (EnumFacing) state.func_177229_b(PROPERTYFACING);
      EnumFacing directionOfNeighbour = enumfacing.func_176734_d();
      if (!adjacentBlockIsASuitableSupport(worldIn, pos, directionOfNeighbour)) {
        this.func_176226_b(worldIn, pos, state, 0);
        worldIn.func_175698_g(pos);
      }
    }
  }

  /**
   * Can we place the block at this location?
   * @param worldIn
   * @param thisBlockPos    the position of this block (not the neighbour)
   * @param faceOfNeighbour the face of the neighbour that is adjacent to this block.  If I am facing east, with a stone
   *                        block to the east of me, and I click on the westward-pointing face of the block,
   *                        faceOfNeighbour is WEST
   * @return true if the block can be placed here
   */
  @Override
  public boolean func_176198_a(World worldIn, BlockPos thisBlockPos, EnumFacing faceOfNeighbour)
  {
    EnumFacing directionOfNeighbour = faceOfNeighbour.func_176734_d();
    if (directionOfNeighbour == EnumFacing.DOWN || directionOfNeighbour == EnumFacing.UP) {
      return false;
    }
    return adjacentBlockIsASuitableSupport(worldIn, thisBlockPos, directionOfNeighbour);
  }

  // Create the appropriate state for the block being placed - in this case, figure out which way the target is facing
  @Override
  public IBlockState func_180642_a(World worldIn, BlockPos thisBlockPos, EnumFacing faceOfNeighbour,
                                   float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
  {
    EnumFacing directionTargetIsPointing = faceOfNeighbour;
//    if

    return this.func_176223_P().func_177226_a(PROPERTYFACING, directionTargetIsPointing);
  }

  // Is the neighbouring block in the given direction suitable for mounting the target onto?
  private boolean adjacentBlockIsASuitableSupport(World world, BlockPos thisPos, EnumFacing directionOfNeighbour)
  {
    BlockPos neighbourPos = thisPos.func_177972_a(directionOfNeighbour);
    EnumFacing neighbourSide = directionOfNeighbour.func_176734_d();
    boolean DEFAULT_SOLID_VALUE = false;
    return world.isSideSolid(neighbourPos, neighbourSide, DEFAULT_SOLID_VALUE);
  }

  //--- methods related to the appearance of the block
  //  See MBE03_block_variants for more explanation

  // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k()
  {
    return BlockRenderLayer.SOLID;
  }

  // used by the renderer to control lighting and visibility of other blocks.
  // set to false because this block doesn't fill the entire 1x1x1 space
  @Override
  public boolean func_149662_c(IBlockState iBlockState)
  {
    return false;
  }

  // used by the renderer to control lighting and visibility of other blocks, also by
  // (eg) wall or fence to control whether the fence joins itself to this block
  // set to false because this block doesn't fill the entire 1x1x1 space
  @Override
  public boolean func_149686_d(IBlockState iBlockState)
  {
    return false;
  }

  // render using a BakedModel
  // not strictly required because the default (super method) is MODEL.
  @Override
  public EnumBlockRenderType func_149645_b(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }

  /**
   * Returns the borders of the target, depends on which way it is facing.
   * Used by the vanilla getCollisionBox.
   * @param state
   * @param source
   * @param pos
   * @return the AxisAlignedBoundingBox of the target, origin at [0,0,0].
   */
  @Override
  public AxisAlignedBB func_185496_a(IBlockState state, IBlockAccess source, BlockPos pos)
  {
    EnumFacing facing = (EnumFacing) state.func_177229_b(PROPERTYFACING);

    switch (facing) {
      case NORTH: {
        return  NORTH_AABB;
      }
      case WEST: {
        return WEST_AABB;
      }
      case EAST: {
        return  EAST_AABB;
      }
      case SOUTH: {
        return SOUTH_AABB;
      }
    }
    return field_185505_j;
  }

  private final AxisAlignedBB NORTH_AABB = getAABBFromPixels(0, 0, 15, 16, 16, 16);
  private final AxisAlignedBB SOUTH_AABB = getAABBFromPixels(0, 0, 0, 16, 16, 1);
  private final AxisAlignedBB EAST_AABB = getAABBFromPixels(0, 0, 0, 1, 16, 16);
  private final AxisAlignedBB WEST_AABB = getAABBFromPixels(15, 0, 0, 16, 16, 16);

  @Override
  public Block func_149722_s() {
    return super.func_149722_s();
  }

  private AxisAlignedBB getAABBFromPixels(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
  {
    final float PIXEL_WIDTH = 1.0F / 16.0F;
    return new AxisAlignedBB(minX * PIXEL_WIDTH, minY * PIXEL_WIDTH, minZ * PIXEL_WIDTH,
                             maxX * PIXEL_WIDTH, maxY * PIXEL_WIDTH, maxZ * PIXEL_WIDTH);
  }
  // ---------methods related to storing information about the block (which way it's facing)

  // BlockRedstoneTarget has only one property:
  //PROPERTYFACING for which way the target points (east, west, north, south).  EnumFacing is a standard used by vanilla for a number of blocks.
  //    eg EAST means that the red and white rings on the target are pointing east
  //
  public static final PropertyDirection PROPERTYFACING = PropertyDirection.func_177712_a("facing", EnumFacing.Plane.HORIZONTAL);

  // getStateFromMeta, getMetaFromState are used to interconvert between the block's property values and
  //   the stored metadata (which must be an integer in the range 0 - 15 inclusive)
  // The property is encoded as:
  // - lower two bits = facing direction (i.e. 0, 1, 2, 3)
  @Override
  public IBlockState func_176203_a(int meta)
  {
    EnumFacing facing = EnumFacing.func_176731_b(meta);
    return this.func_176223_P().func_177226_a(PROPERTYFACING, facing);
  }

  @Override
  public int func_176201_c(IBlockState state)
  {
    EnumFacing facing = (EnumFacing)state.func_177229_b(PROPERTYFACING);

    int facingbits = facing.func_176736_b();
    return facingbits;
  }

  // this method isn't required if your properties only depend on the stored metadata.
  // it is required if:
  // 1) you are making a multiblock which stores information in other blocks eg BlockBed, BlockDoor
  // 2) your block's state depends on other neighbours (eg BlockFence)
  @Override
  public IBlockState func_176221_a(IBlockState state, IBlockAccess worldIn, BlockPos pos)
  {
    return state;
  }

  // necessary to define which properties your blocks use
  // will also affect the variants listed in the blockstates model file
  @Override
  protected BlockStateContainer func_180661_e()
  {
    return new BlockStateContainer(this, new IProperty[] {PROPERTYFACING});
  }
}
