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
	
	public static BlockFace of(BlockPos pos, EnumFacing face) {
		return new BlockFace(pos, face);
	}
	
	public static BlockFace up(BlockPos pos) {
		return of(pos, EnumFacing.UP);
	}
	public static BlockFace down(BlockPos pos) {
		return of(pos, EnumFacing.DOWN);
	}
	public static BlockFace east(BlockPos pos) {
		return of(pos, EnumFacing.EAST);
	}
	public static BlockFace west(BlockPos pos) {
		return of(pos, EnumFacing.WEST);
	}
	public static BlockFace north(BlockPos pos) {
		return of(pos, EnumFacing.NORTH);
	}
	public static BlockFace south(BlockPos pos) {
		return of(pos, EnumFacing.SOUTH);
	}
	
	public BlockFace otherSide() {
		return of(pos.offset(getFacing()), getFacing().getOpposite());
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
