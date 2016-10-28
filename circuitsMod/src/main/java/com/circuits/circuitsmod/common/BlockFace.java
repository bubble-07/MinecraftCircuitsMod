package com.circuits.circuitsmod.common;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * A block position, facing combination which can uniquely identify a face of a block
 * @author bubble-07
 *
 */
public class BlockFace {
	private final EnumFacing face;
	private final BlockPos pos;
	public BlockFace(BlockPos pos, EnumFacing face) {
		this.pos = pos;
		this.face = face;
	}
	public EnumFacing getFacing() {
		return face;
	}
	public BlockPos getPos() {
		return pos;
	}
	public BlockPos adjacent() {
		return pos.offset(face);
	}
	public String toString() {
		return getPos().toString() + ":" + getFacing().toString();
	}
}
