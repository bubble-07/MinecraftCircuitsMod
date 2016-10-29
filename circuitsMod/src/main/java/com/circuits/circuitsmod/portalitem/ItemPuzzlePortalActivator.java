package com.circuits.circuitsmod.portalitem;

import com.circuits.circuitsmod.blockportalpuzzle.StartupCommonPortal;

import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemPuzzlePortalActivator extends Item{
	 
	public ItemPuzzlePortalActivator() {
		this.setMaxStackSize(64);
		this.setUnlocalizedName("puzzle_portal_activator");
		this.setCreativeTab(CreativeTabs.TOOLS);
	}
	
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
    	int x = pos.getX();
    	int y = pos.getY();
    	int z = pos.getZ();
    	int side = facing.getIndex();
    	
    	if(side == 0) {
    		y--;
    	}
    	if(side == 1) {
    		y++;
    	}
    	if(side == 2) {
    		z--;
    	}
    	if(side == 3) {
    		z++;
    	}
    	if(side == 4) {
    		x--;
    	}
    	if(side == 5) {
    		x++;
    	}
    	if(!playerIn.canPlayerEdit(new BlockPos(x, y, z), facing, stack)) {
    		return EnumActionResult.FAIL;
    	}
    	IBlockState location = worldIn.getBlockState(new BlockPos(x, y, z));
    	if(location == Blocks.AIR.getDefaultState()) {
    		worldIn.playSound(playerIn, new BlockPos(x, y, z), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.BLOCKS, 1.0f, itemRand.nextFloat() * 0.4f + 0.8f);
    		StartupCommonPortal.blockPortalPuzzle.trySpawnPortal(worldIn, new BlockPos(x, y, z));
    	}
    	stack.stackSize--;
    	return EnumActionResult.SUCCESS;
    }
}