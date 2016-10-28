package com.circuits.circuitsmod.circuitblock;


import com.circuits.circuitsmod.CircuitsMod;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CircuitItem extends ItemBlock {
	private final String name = "circuititem";
	
	@SideOnly(Side.CLIENT)
	public CircuitSmartModel renderer;
	
	public CircuitItem(Block block) { 
		super(block);
		setUnlocalizedName(CircuitsMod.MODID + "_" + name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setRenderer(CircuitSmartModel itemRenderer) {
		this.renderer = itemRenderer;
	}
	
	@Override
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ).equals(EnumActionResult.SUCCESS)) {
			//Block placed
			CircuitTileEntity tileEntity = (CircuitTileEntity)worldIn.getTileEntity(pos);
			if (tileEntity != null) {
				tileEntity.init(worldIn, stack.getDisplayName());
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}
}
