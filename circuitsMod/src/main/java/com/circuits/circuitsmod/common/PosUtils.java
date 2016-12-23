package com.circuits.circuitsmod.common;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PosUtils {
	public static Stream<BlockPos> neighbors(BlockPos pos) {
		return faces(pos).map((bf) -> bf.adjacent());
	}
	/**
	 * Returns all BlockFaces of the block at the specified position
	 * @param pos
	 * @return
	 */
	public static Stream<BlockFace> faces(BlockPos pos) {
		return Stream.of(EnumFacing.values()).map((f) -> new BlockFace(pos, f));
	}
	
	/**
	 * Returns all BlockFaces of other blocks that are adjacent to this one.
	 * @param pos
	 * @return
	 */
	public static Stream<BlockFace> adjacentFaces(BlockPos pos) {
		return Stream.of(EnumFacing.values()).map((f) -> new BlockFace(pos.offset(f), f.getOpposite()));
	}
	
	public static Stream<Pair<BlockPos, IBlockState>> toPosState(Stream<BlockPos> posStream, World worldIn) {
		return posStream.map((p) -> Pair.of(p, worldIn.getBlockState(p)));
	}
	
	public static Optional<BlockPos> searchWithin(BlockPos init, Predicate<BlockPos> safe, Predicate<BlockPos> success) {
		return GraphUtils.generalSearch(init, (p) -> neighbors(p).filter(safe), success);
	}
	
	public static void forBlockPosIn(World worldIn, AxisAlignedBB box, Consumer<BlockPos> f) {
		for (int x = (int) box.minX; x <= (int) box.maxX; x++) {
			for (int y = (int) box.minY; y <= (int) box.maxY; y++) {
				for (int z = (int) box.minZ; z <= (int) box.maxZ; z++) {
					
					BlockPos pos = new BlockPos(x, y, z);
					f.accept(pos);
				}
			}
		}
	}
	
	public static void forBlockIn(World worldIn, AxisAlignedBB box, Consumer<IBlockState> f) {	
		forBlockPosIn(worldIn, box, (pos) -> f.accept(worldIn.getBlockState(pos)));
	}
}
