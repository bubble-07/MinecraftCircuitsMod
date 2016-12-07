package com.circuits.circuitsmod.common;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class RedstoneUtils {
	public boolean isSidePowered(World worldIn, BlockPos origin, EnumFacing side) {
		return getSidePower(worldIn, origin, side) > 0;
	}

	public int getSidePower(World worldIn, BlockPos origin, EnumFacing side) {
		BlockPos pos = origin.offset(side);
		if (worldIn.getRedstonePower(pos, side) > 0) {
			return worldIn.getRedstonePower(pos, side);
		}
		else {
			IBlockState iblockstate1 = worldIn.getBlockState(pos);
			return iblockstate1.getBlock() == Blocks.REDSTONE_WIRE ? ((Integer)iblockstate1.getValue(BlockRedstoneWire.POWER)).intValue() : 0;
		}
	}
}
