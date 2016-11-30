package com.circuits.circuitsmod.testblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.reflective.TestGeneratorInvoker;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;

@SuppressWarnings("unused")
public class TileEntityTesting extends TileEntity implements ITickable {
	
	private TestGeneratorInvoker testInvoker;
	private int levelID;
	private final String name = "tileentitytesting";
	private BusSegment segment;
	private BlockFace inputFace;
	
	private CircuitBlock emitterBlock;
	
	private int[] redstoneOutputs = new int[EnumFacing.values().length];
	
	public int getLevelID() {
		return this.levelID;
	}
	
	public void init(World worldIn, int levelID) {
		this.levelID = levelID;
	}
	
	public void update() {
		//Send signal back to emitter
		//monitor incoming signals
		//determine which test to run, in the most BRUTE FORCE WAY POSSIBLE HOLY SHIT!
		if (!getWorld().isRemote) {
			switch(levelID) {
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			case 5:
				break;
			case 6:
				break;
			case 7:
				break;
			case 8:
				break;
			case 9:
				break;
			case 10:
				break;
			case 11:
				break;
			case 12:
				break;
			case 13:
				break;
			case 14:
				break;
			case 15:
				break;
			case 16:
				break;
			case 17:
				break;
			case 18:
				break;
			case 19:
				break;
			case 20:
				break;
			}
		}
	}
	
	public void findNearestEmitter() {
		/**
		 * A predicate function to determine if the block is safe to search.
		 * A block is safe if it's less than 128 units away from the start.
		 */
		Predicate<BlockPos> safe = pos-> {
			return pos.getDistance(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) > 128;
		};
		
		/*
		 * A predicate to determine if we've found an emitter.
		 * Returns true if we've found a circuit tile entity and its UID matches the emitters UID.
		 */
		Predicate<BlockPos> success = pos-> {
			TileEntity entity = getWorld().getTileEntity(pos);
			if (entity instanceof CircuitTileEntity) {
				CircuitTileEntity circuitEntity = (CircuitTileEntity) entity;
				if (circuitEntity.getCircuitUID().toInteger() == 15) {
					return true;
				} else return false;
			}
			else return false;
		};
		
		//Search for the correct block position
		Optional<BlockPos> candidatePos = PosUtils.searchWithin(this.getPos(), safe, success);
		
		if (candidatePos.isPresent()) {
			BlockPos position = candidatePos.get();
			Stream<BlockFace> faces = PosUtils.faces(position);
			List<BlockFace> faceList = faces.collect(Collectors.toList());
			BusSegment maximumSegment = new BusSegment(0);
			int maxWidth = 0;
			//Find the face with the largest bus width.  This is our input.
			for (BlockFace face : faceList) {
				Optional<BusSegment> currentSegment = CircuitBlock.getBusSegmentAt(getWorld(), face);
				BusSegment segment = currentSegment.get();
				if (segment.getWidth() > maxWidth)
					maximumSegment = currentSegment.get();
			}
			segment = maximumSegment; //remember the segment
			BlockFace inputFace = new BlockFace(getPos(), EnumFacing.NORTH); //bind an input to it.
			this.inputFace = inputFace; //remember the input
			segment.addInput(inputFace); //add it to the segment.
		}
			
	}
	
	
	boolean isSidePowered(EnumFacing side) {
		return getSidePower(side) > 0;
	}
	
	int getSidePower(EnumFacing side) {
		BlockPos pos = getPos().offset(side);
		if (getWorld().getRedstonePower(pos, side) > 0) {
			return getWorld().getRedstonePower(pos, side);
		}
		else {
			IBlockState iblockstate1 = getWorld().getBlockState(pos);
			return iblockstate1.getBlock() == Blocks.REDSTONE_WIRE ? ((Integer)iblockstate1.getValue(BlockRedstoneWire.POWER)).intValue() : 0;
		}
	}
	
	public EnumFacing getParentFacing() {
		IBlockState parentState = getWorld().getBlockState(getPos());
		return (EnumFacing)parentState.getValue(BlockDirectional.FACING);
	}
	
}
