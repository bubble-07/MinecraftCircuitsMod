package minecraftbyexample.mbe20_tileentity_data;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * User: The Grey Ghost
 * Date: 11/01/2015
 *
 * BlockTileEntityData is a ordinary solid cube with an associated TileEntity
 * For background information on blocks see here http://greyminecraftcoder.blogspot.com.au/2014/12/blocks-18.html
 * You can either make your block implement ITileEntityProvider, or alternatively
 * @Override hasTileEntity(IBlockState state)
 */
public class BlockTileEntityData extends Block implements ITileEntityProvider
{
  public BlockTileEntityData()
  {
    super(Material.field_151576_e);
    this.func_149647_a(CreativeTabs.field_78030_b);   // the block will appear on the Blocks tab in creative
  }

  private final int TIMER_COUNTDOWN_TICKS = 20 * 10; // duration of the countdown, in ticks = 10 seconds

  // Called when the block is placed or loaded client side to get the tile entity for the block
  // Should return a new instance of the tile entity for the block
  // Alternatively - you can @Override hasTileEntity(IBlockState state) and
  //    createTileEntity(World world, IBlockState state) instead.
  @Override
  public TileEntity func_149915_a(World worldIn, int meta) {
    return new TileEntityData();
  }

  // Called just after the player places a block.  Start the tileEntity's timer
  @Override
  public void func_180633_a(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    super.func_180633_a(worldIn, pos, state, placer, stack);
    TileEntity tileentity = worldIn.func_175625_s(pos);
    if (tileentity instanceof TileEntityData) { // prevent a crash if not the right type, or is null
      TileEntityData tileEntityData = (TileEntityData)tileentity;
      tileEntityData.setTicksLeftTillDisappear(TIMER_COUNTDOWN_TICKS);
    }
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

  // render using a BakedModel
  // not required because the default (super method) is MODEL
  @Override
  public EnumBlockRenderType func_149645_b(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }
}
