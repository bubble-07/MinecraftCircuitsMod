package com.circuits.circuitsmod.common;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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
	
	public static AxisAlignedBB getBBoxFacet(AxisAlignedBB box, EnumFacing facing) {
		
		switch (facing) {
		case DOWN:
			return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ);
		case EAST:
			return new AxisAlignedBB(box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
		case NORTH:
			return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ);
		case SOUTH:
			return new AxisAlignedBB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ);
		case UP:
			return new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ);
		case WEST:
			return new AxisAlignedBB(box.minX, box.minY, box.minZ, box.minX, box.maxY, box.maxZ);
		default:
			break;
		}
		return box;
	}
	
	public static AxisAlignedBB shrinkBBox(AxisAlignedBB box, double amount) {
		double xDiffDiv = (box.maxX - box.minX) / 2;
		double yDiffDiv = (box.maxY - box.minY) / 2;
		double zDiffDiv = (box.maxZ - box.minZ) / 2;
		return box.expand(-Math.min(amount, xDiffDiv), -Math.min(amount, yDiffDiv), -Math.min(amount, zDiffDiv));
	}
	
	public static Stream<BlockPos> streamBlockPosIn(AxisAlignedBB box) {
		return IntStream.rangeClosed((int) box.minX, (int) box.maxX).boxed()
		         .flatMap((x) -> IntStream.rangeClosed((int) box.minY, (int) box.maxY).boxed()
		        		         .flatMap((y) -> IntStream.rangeClosed((int) box.minZ, (int) box.maxZ).mapToObj((z) -> new BlockPos(x, y, z))));
	}
	
	public static Stream<BlockPos> extremalPosIn(AxisAlignedBB box) {
		//TODO: Rename to 1dfacesof or something
		return streamBlockPosIn(box).filter((pos) -> {
			int extremalCount = ((pos.getX() == (int) box.maxX || pos.getX() == (int) box.minX) ? 1 : 0) +
					            ((pos.getY() == (int) box.maxY || pos.getY() == (int) box.minY) ? 1 : 0) +
					            ((pos.getZ() == (int) box.maxZ || pos.getZ() == (int) box.minZ) ? 1 : 0);
			
			return extremalCount > 1;
		});
	}
	
	public static void forBlockPosIn(AxisAlignedBB box, Consumer<BlockPos> f) {
		streamBlockPosIn(box).forEach(f);
	}
	
	public static void forBlockIn(World worldIn, AxisAlignedBB box, Consumer<IBlockState> f) {	
		forBlockPosIn(box, (pos) -> f.accept(worldIn.getBlockState(pos)));
	}
}
