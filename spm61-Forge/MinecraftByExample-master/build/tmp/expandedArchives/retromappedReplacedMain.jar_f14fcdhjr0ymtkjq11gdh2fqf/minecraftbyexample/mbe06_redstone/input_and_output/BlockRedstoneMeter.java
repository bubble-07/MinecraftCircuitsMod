package minecraftbyexample.mbe06_redstone.input_and_output;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * User: The Grey Ghost
 * Date: 27/11/2015
 *
 * BlockRedstoneMeter is a simple block with an associated TileEntity to render the block's power level.
 * The meter will also provide weak power to the blocks UP and DOWN (eg a light) - it flashes the light
 *   at a speed related to the input power.
 * We use a TileEntity because
 * 1) that's the easiest way to get the block's power level on the client side, without
 *   having to use metadata.
 * 2) our block needs to store the input power level, for later use when others call the getWeakPower().
 *    for the reason why, see http://greyminecraftcoder.blogspot.com.au/2015/11/redstone.html
 */
public class BlockRedstoneMeter extends Block implements ITileEntityProvider
{
  public BlockRedstoneMeter()
  {
    super(Material.field_151573_f);
    this.func_149647_a(CreativeTabs.field_78030_b);   // the block will appear on the Blocks tab in creative
  }

  // Called when the block is placed or loaded client side to get the tile entity for the block
  // Should return a new instance of the tile entity for the block
  @Override
  public TileEntity func_149915_a(World worldIn, int meta) {
    return new TileEntityRedstoneMeter();
  }

  // ------ methods relevant to redstone
  //  The methods below are used to provide power to neighbours.
  //  If you are looking for the rendering redstone calculations, look in TileEntityRedstoneMeter.getPowerLevelClient()

  /**
   * This block can provide power
   * @return
   */
  @Override
  public boolean func_149744_f(IBlockState iBlockState)
  {
    return true;
  }

  /** How much weak power does this block provide to the adjacent block?
   * The meter flashes the power according to how strong the input signals are
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
    if (side != EnumFacing.UP && side != EnumFacing.DOWN) {
      return 0;
    }

    boolean isOutputOn = false;
    TileEntity tileentity = worldIn.func_175625_s(pos);
    if (tileentity instanceof TileEntityRedstoneMeter) { // prevent a crash if not the right type, or is null
      TileEntityRedstoneMeter tileEntityRedstoneMeter = (TileEntityRedstoneMeter) tileentity;
      isOutputOn = tileEntityRedstoneMeter.getOutputState();
    }

    final int OUTPUT_POWER_WHEN_ON = 15;
    return isOutputOn ? OUTPUT_POWER_WHEN_ON : 0;
  }

  /**
   *  The redstone meter doesn't provide strong power to any other block.
   * @param worldIn
   * @param pos the position of this block
   * @param state the blockstate of this block
   * @param side the side of the block - eg EAST means that this is to the EAST of the adjacent block.
   * @return The power provided [0 - 15]
   */

  @Override
  public int func_176211_b(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
  {
    return 0;
  }

  // Retrieve the current input power level of the meter - the maximum of the four sides EAST, WEST, NORTH, SOUTH
  //   (don't look UP or DOWN)
  private int getPowerLevelInput(World world, BlockPos pos) {

//    int powerLevel = world.isBlockIndirectlyGettingPowered(pos);  // if input can come from any side, use this line

    int maxPowerFound = 0;
    for (EnumFacing whichFace : EnumFacing.field_176754_o) {
      BlockPos neighborPos = pos.func_177972_a(whichFace);
      int powerLevel = world.func_175651_c(neighborPos, whichFace);
      maxPowerFound = Math.max(powerLevel, maxPowerFound);
    }
    return maxPowerFound;
  }

  // ------ various block methods that react to changes and are responsible for updating the redstone power information

  // Called just after the player places a block.
  // Only called on the server side so it doesn't help us alter rendering on the client side.
  @Override
  public void func_180633_a(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.func_180633_a(worldIn, pos, state, placer, stack);
  }

  // Called when a neighbouring block changes.
  // Only called on the server side- so it doesn't help us alter rendering on the client side.
  @Override
  public void func_189540_a(IBlockState state, World worldIn, BlockPos pos, Block neighborBlock)
  {
    // calculate the power level from neighbours and store in our TileEntity for later use in isProvidingWeakPower()
    int powerLevel = getPowerLevelInput(worldIn, pos);
    TileEntity tileentity = worldIn.func_175625_s(pos);
    if (tileentity instanceof TileEntityRedstoneMeter) { // prevent a crash if not the right type, or is null
      TileEntityRedstoneMeter tileEntityRedstoneMeter = (TileEntityRedstoneMeter) tileentity;

      boolean currentOutputState = tileEntityRedstoneMeter.getOutputState();
      tileEntityRedstoneMeter.setPowerLevel(powerLevel);
          // this method will also schedule the next tick using call world.scheduleUpdate(pos, block, lastTickDelay);

      if (currentOutputState != tileEntityRedstoneMeter.getOutputState()) {
        worldIn.func_175685_c(pos, this);
      }
    }
  }

  // Our flashing output uses scheduled ticks to toggle the output.
  //  Scheduling of ticks is by calling  world.scheduleUpdate(pos, block, numberOfTicksToDelay);
  //
  @Override
  public void func_180650_b(World worldIn, BlockPos pos, IBlockState state, Random rand)
  {
    TileEntity tileentity = worldIn.func_175625_s(pos);
    if (tileentity instanceof TileEntityRedstoneMeter) { // prevent a crash if not the right type, or is null
      TileEntityRedstoneMeter tileEntityRedstoneMeter = (TileEntityRedstoneMeter) tileentity;

      boolean currentOutputState = tileEntityRedstoneMeter.getOutputState();
      tileEntityRedstoneMeter.onScheduledUpdateTick();
        // this method will also schedule the next tick using call world.scheduleUpdate(pos, block, lastTickDelay);

      if (currentOutputState != tileEntityRedstoneMeter.getOutputState()) {
        worldIn.func_175685_c(pos, this);
      }
    }
  }

  // ---- the following are copied from BlockRedstoneComparator.  I'm not 100% sure it's necessary to manually
  //   setTileEntity, removeTileEntity, etc, but I figure copying vanilla is a good rule
  @Override
  public void func_176213_c(World worldIn, BlockPos pos, IBlockState state)
  {
    super.func_176213_c(worldIn, pos, state);
    worldIn.func_175690_a(pos, this.func_149915_a(worldIn, 0));
  }

  public void func_180663_b(World worldIn, BlockPos pos, IBlockState state)
  {
    super.func_180663_b(worldIn, pos, state);
    worldIn.func_175713_t(pos);
    worldIn.func_175685_c(pos, this);
  }

  // -----------------
  // The following methods aren't particularly relevant to this example.  See MBE01, MBE02, MBE03 for more information.
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer func_180664_k()
  {
    return BlockRenderLayer.CUTOUT_MIPPED;
  }

  // used by the renderer to control lighting and visibility of other blocks.
  // set to true because this block is opaque and occupies the entire 1x1x1 space
  // not strictly required because the default (super method) is true
  @Override
  public boolean func_149662_c(IBlockState iBlockState) {
    return true;
  }

  // used by the renderer to control lighting and visibility of other blocks, also by
  // (eg) wall or fence to control whether the fence joins itself to this block
  // set to true because this block occupies the entire 1x1x1 space
  // not strictly required because the default (super method) is true
  @Override
  public boolean func_149686_d(IBlockState iBlockState) {
    return true;
  }

  // render using a BakedModel (mbe01_block_simple.json --> mbe01_block_simple_model.json)
  // not strictly required because the default (super method) is MODEL.
  @Override
  public EnumBlockRenderType func_149645_b(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }
}
