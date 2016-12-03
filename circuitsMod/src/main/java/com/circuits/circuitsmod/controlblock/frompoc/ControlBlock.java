package com.circuits.circuitsmod.controlblock.frompoc;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ControlBlock extends BlockContainer {
	private final String name = "controlBlock";
	public ControlBlock() {
		super(Material.GROUND);
		GameRegistry.registerBlock(this, name);
		setUnlocalizedName(Microchips.MODID + "_" + name);
		setCreativeTab(CreativeTabs.tabBlock);
		setHardness(0.5F);
		setStepSound(Block.soundTypeAnvil);
	}
	public String getName() {
		return name;
	}
	@Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, 
    		    EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (tileEntity == null) {
			return false;
		}
		player.openGui(Microchips.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		
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
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);
			if (item != null && item.stackSize > 0) {
				EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ());
				if (item.hasTagCompound()) {
					entityItem.getEntityItem().setTagCompound((NBTTagCompound) item.getTagCompound().copy());
				}
				world.spawnEntityInWorld(entityItem);
				item.stackSize = 0;
			}
		}
	}
	@Override
	public TileEntity createNewTileEntity(World world, int ignored) {
		return new ControlTileEntity();
	}
}
