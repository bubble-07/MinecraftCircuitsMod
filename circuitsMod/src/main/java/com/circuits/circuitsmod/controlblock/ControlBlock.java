package com.circuits.circuitsmod.controlblock;

import java.util.Optional;

import javax.annotation.Nullable;

import com.circuits.circuitsmod.CircuitsMod;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


public class ControlBlock extends BlockContainer
{   
	public ControlBlock()
	{
		super(Material.ROCK);
		this.setHardness(10);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);     // the block will appear on the Blocks tab.
	}
	
	@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, 
    		    EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity == null) {
			return false;
		}
		if (!worldIn.isRemote) {
			player.openGui(CircuitsMod.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		dropItems(worldIn, pos);
		super.breakBlock(worldIn, pos, state);
	}
	private void dropItems(World world, BlockPos pos) {
		TileEntity tileEntity = world.getTileEntity(pos);
		IInventory inventory = (IInventory) tileEntity;
		//Clear the crafting slot
		inventory.setInventorySlotContents(7, null);
		InventoryHelper.dropInventoryItems(world, pos, inventory);
		world.updateComparatorOutputLevel(pos, this);
	}
	@Override
	public TileEntity createNewTileEntity(World world, int ignored) {
		return new ControlTileEntity();
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
	
  
	public static Optional<ControlTileEntity> getControlTileEntityAt(IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null) {
			return Optional.empty();
		}
		try {
			return Optional.of((ControlTileEntity) te);
		}
		catch (ClassCastException e) {
			return Optional.empty();
		}
	}
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}
}
