package com.circuits.circuitsmod.telecleaner;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Teleporter [next puzzle]/inventory cleaner for the puzzle dimension
 * @author spm61
 *
 */
public class TeleCleaner extends Block
{
  public TeleCleaner()
  {
    super(Material.ROCK);
    this.setHardness(9999);
    //bubble-07: Puzzle dimension features delayed for future release. It's more important to get the overworld functionality out there for now,
    //fix bugs, and solicit feedback from redstoners to achieve a good degree of polish while the puzzle dimension levels are completed.
    //this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);   // the block will appear on the Blocks tab in creative
  }

  @Override
  public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
	  if (!playerIn.isRiding() && !playerIn.isBeingRidden() && playerIn.isNonBoss() && playerIn instanceof EntityPlayerMP) {
		  EntityPlayerMP thePlayer = (EntityPlayerMP) playerIn;
		  thePlayer.inventory.clear();
		  EnumFacing direction = thePlayer.getAdjustedHorizontalFacing();
		  
		  if (direction == EnumFacing.EAST){ 
			  thePlayer.setPositionAndUpdate(thePlayer.posX + 128, thePlayer.posY, thePlayer.posZ);
			  return true;
		  }
		  if (direction == EnumFacing.WEST) {
			  thePlayer.setPositionAndUpdate(thePlayer.posX - 128, thePlayer.posY, thePlayer.posZ);
			  return true;
		  }
		  if (direction == EnumFacing.NORTH) {
			  thePlayer.setPositionAndUpdate(thePlayer.posX, thePlayer.posY, thePlayer.posZ - 128);
			  return true;
		  }
		  if (direction == EnumFacing.SOUTH) {
			  thePlayer.setPositionAndUpdate(thePlayer.posX, thePlayer.posY, thePlayer.posZ + 128);
			  return true;
		  }
		 
	  }
	  return false;
  }
  
  
  // the block will render in the SOLID layer.  See http://greyminecraftcoder.blogspot.co.at/2014/12/block-rendering-18.html for more information.
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer()
  {
    return BlockRenderLayer.SOLID;
  }

  // used by the renderer to control lighting and visibility of other blocks.
  // set to true because this block is opaque and occupies the entire 1x1x1 space
  // not strictly required because the default (super method) is true
  @Override
  public boolean isOpaqueCube(IBlockState iBlockState) {
    return true;
  }

  // used by the renderer to control lighting and visibility of other blocks, also by
  // (eg) wall or fence to control whether the fence joins itself to this block
  // set to true because this block occupies the entire 1x1x1 space
  // not strictly required because the default (super method) is true
  @Override
  public boolean isFullCube(IBlockState iBlockState) {
    return true;
  }

  // render using a BakedModel (mbe01_block_simple.json --> mbe01_block_simple_model.json)
  // not strictly required because the default (super method) is MODEL.
  @Override
  public EnumBlockRenderType getRenderType(IBlockState iBlockState) {
    return EnumBlockRenderType.MODEL;
  }
} 
