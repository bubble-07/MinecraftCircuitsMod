package com.circuits.circuitsmod.unbreakium;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class UnbreakiumBlock extends Block
{
  public UnbreakiumBlock()
  {
    super(Material.ROCK);
    this.setHardness(9999f);
    this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
	this.setDefaultState(this.blockState.getBaseState().withProperty(FRAMEMIMIC, false));
  }
  
  public static final IProperty<Boolean> FRAMEMIMIC = PropertyBool.create("framemimic");

  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer()
  {
    return BlockRenderLayer.TRANSLUCENT;
  }

  @Override
  public boolean isOpaqueCube(IBlockState iBlockState) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState iBlockState) {
    return true;
  }
  
  @Override
  protected BlockStateContainer createBlockState() {
	  return new BlockStateContainer(this, new IProperty[]{FRAMEMIMIC});
  }

  /**
   * Convert the given metadata into a BlockState for this Block
   */
  public IBlockState getStateFromMeta(int meta)
  {
	  return this.getDefaultState().withProperty(FRAMEMIMIC, meta > 0);
  }

  /**
   * Convert the BlockState into the correct metadata value
   */
  public int getMetaFromState(IBlockState state)
  {
	  return state.getValue(FRAMEMIMIC) ? 1 : 0;
  }

  @Override
  public int damageDropped(IBlockState state) {
	  return getMetaFromState(state);
  }
  
  @Override
  public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
	  worldIn.scheduleUpdate(pos, this, 1);
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }
} 
