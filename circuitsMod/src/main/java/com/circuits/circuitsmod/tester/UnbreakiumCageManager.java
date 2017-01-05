package com.circuits.circuitsmod.tester;

import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.controlblock.StartupCommonControl;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.unbreakium.StartupCommonUnbreakium;
import com.circuits.circuitsmod.unbreakium.UnbreakiumBlock;

public class UnbreakiumCageManager<TEType extends TileEntity> {
	private CircuitSequenceReader<TEType, ?> seqReader;
	public UnbreakiumCageManager(CircuitSequenceReader<TEType, ?> seqReader) {
		this.seqReader = seqReader;
	}
	

	
	public void setupUnbreakiumCage() {
		if (seqReader.getBBox() == null) {
			return;
		}
		unbreakiumCageStream().forEach((pos) -> seqReader.getParent().getWorld().setBlockState(pos, StartupCommonUnbreakium.unbreakiumBlock.getDefaultState(), 3));
		unbreakiumFrameStream(seqReader.getBBox()).forEach((pos) -> seqReader.getParent().getWorld().setBlockState(pos, 
				                                 StartupCommonUnbreakium.unbreakiumBlock.getDefaultState().withProperty(UnbreakiumBlock.FRAMEMIMIC, true), 3));
		
	}
	
	public void tearDownUnbreakiumCage() {
		if (seqReader.getBBox() == null) {
			return;
		}
		unbreakiumCageSidesStream().forEach((pos) -> {
			seqReader.getParent().getWorld().setBlockToAir(pos);
		});
		unbreakiumCageBottomStream().forEach((pos) -> {
			seqReader.getParent().getWorld().setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
		});
		unbreakiumFrameStream(seqReader.getBBox()).forEach((pos) -> {
			//Replace frame mimics with real frame blocks again
			if (isFrame(pos)) {
				seqReader.getParent().getWorld().setBlockState(pos, StartupCommonFrame.frameBlock.getDefaultState(), 3);
			}
		});
	}
	
	
	/**
	 * Returns true if the block is a frame or an unbreakable frame mimic
	 * @param pos
	 * @return
	 */
	private boolean isFrame(BlockPos pos) {
		IBlockState state = seqReader.getWorld().getBlockState(pos);
		return (state.getBlock() == StartupCommonFrame.frameBlock) || 
				(state.getBlock() == StartupCommonUnbreakium.unbreakiumBlock && state.getValue(UnbreakiumBlock.FRAMEMIMIC));
	}
	
	private Stream<BlockPos> unbreakiumCageBottomStream() {
		AxisAlignedBB bottomTestFace = PosUtils.getBBoxFacet(seqReader.getBBox(), EnumFacing.DOWN);
		
		Stream<AxisAlignedBB> bottomSides = Stream.of(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH)
		      .map((f) -> PosUtils.getBBoxFacet(bottomTestFace, f).offset(0.0, -1.0, 0.0));
		
		AxisAlignedBB bottomFace = bottomTestFace.offset(0.0, -2.0, 0.0);
		
		return Stream.concat(Stream.of(bottomFace), bottomSides).flatMap((bb) -> PosUtils.streamBlockPosIn(bb));
	}
	
	private Stream<BlockPos> unbreakiumFrameStream(AxisAlignedBB box) {
		return PosUtils.extremalPosIn(box)
				       .filter((pos) -> seqReader.getParent().getWorld().getBlockState(pos).getBlock() != StartupCommonControl.controlBlock);
	}
	
	private Stream<BlockPos> unbreakiumCageSidesStream() {
		

		
		Stream<AxisAlignedBB> otherSides = Stream.of(EnumFacing.values()).filter((f) -> !f.equals(EnumFacing.DOWN))
		                                         .map((f) -> PosUtils.shrinkBBox(PosUtils.getBBoxFacet(seqReader.getBBox(), f), 1.0));
		
		return otherSides.flatMap((bb) -> PosUtils.streamBlockPosIn(bb));
	}
	
	private Stream<BlockPos> unbreakiumCageStream() {
		return Stream.concat(unbreakiumCageBottomStream(), unbreakiumCageSidesStream());
	}
	
	public Optional<AxisAlignedBB> getTestingBox() {
		
		//Get the vertical extent
		int vertExtent = 0;
		while (isFrame(seqReader.getParent().getPos().up(vertExtent + 1))) {
			vertExtent++;
		}
		int pos_x_extent = 0;
		while (isFrame(seqReader.getParent().getPos().add(pos_x_extent + 1, 0, 0))) {
			pos_x_extent++;
		}
		int neg_x_extent = 0;
		while (isFrame(seqReader.getParent().getPos().add(-neg_x_extent - 1, 0, 0))) {
			neg_x_extent++;
		}
		int pos_z_extent = 0;
		while (isFrame(seqReader.getParent().getPos().add(0, 0, pos_z_extent + 1))) {
			pos_z_extent++;
		}
		int neg_z_extent = 0;
		while (isFrame(seqReader.getParent().getPos().add(0, 0, -neg_z_extent - 1))) {
			neg_z_extent++;
		}
		
		AxisAlignedBB bbox = new AxisAlignedBB(seqReader.getParent().getPos().add(-neg_x_extent, 0, -neg_z_extent), 
			     seqReader.getParent().getPos().add(pos_x_extent, vertExtent, pos_z_extent));
		//If the box ain't big enough, fail.
		if (bbox.maxX - bbox.minX < 2 || bbox.maxY - bbox.minY < 2 || bbox.maxZ - bbox.minZ < 2) {
			return Optional.empty();
		}
		//The above was just a quick determination of extents. Make sure they actually built a frame
		if (!unbreakiumFrameStream(bbox).allMatch(this::isFrame)) {
			return Optional.empty();
		}
		
		return Optional.of(bbox);
	}
	
}