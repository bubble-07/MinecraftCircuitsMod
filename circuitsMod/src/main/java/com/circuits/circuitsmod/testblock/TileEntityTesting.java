package com.circuits.circuitsmod.testblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
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
import com.circuits.circuitsmod.telecleaner.StartupCommonCleaner;
import com.circuits.circuitsmod.testingclasses.PuzzleTest;
import com.circuits.circuitsmod.testingclasses.TestAnd;
import com.circuits.circuitsmod.testingclasses.TestTickResult;

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
	
	private boolean initialized = false;
	private boolean startTesting = false;
	
	private HashMap<Integer, PuzzleTest> testMap = new HashMap<Integer, PuzzleTest>();
	
	private int[] redstoneOutputs = new int[EnumFacing.values().length];
	
	public static int getSidePower(TileEntityTesting testEntity, EnumFacing side) {
		return testEntity.getSidePower(side);
	}
	
	public static boolean isSidePowered(TileEntityTesting testEntity, EnumFacing side) {
		return testEntity.isSidePowered(side);
	}
	
	public void beginTesting(boolean startTesting) {
		this.startTesting = startTesting;
	}
	
	public int getLevelID() {
		return this.levelID;
	}
	
	public BusSegment getBusSegment() {
		return segment;
	}
	
	public BlockFace getInputFace() {
		return inputFace;
	}
	
	public void init(World worldIn, int levelID) {
		if (!getWorld().isRemote) {
			this.levelID = levelID;
			produceHashMap();
			findNearestEmitter();
			initialized = true;
		}
	}
	
	public void produceHashMap() {
		testMap.put(Integer.valueOf(0), new TestAnd());
	}
	
	public void update() {
		//Send signal back to emitter
		//monitor incoming signals
		
		if (!initialized || !startTesting)
			return;
		else if (initialized && startTesting) {
		PuzzleTest toRun = testMap.get(levelID);
		TestTickResult result = toRun.test(getWorld(), this);
		if (result.getAtEndOfTest() && result.getCurrentlySucceeding())
			spawnTeleCleaner();
		}
	}
	
	@Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }
	
	private void spawnTeleCleaner() {
		getWorld().setBlockState(getPos(), StartupCommonCleaner.teleCleaner.getDefaultState(), 1);
	}

	public void findNearestEmitter() {
		/**
		 * A predicate function to determine if the block is safe to search.
		 * A block is safe if it's less than 128 units away from the start.
		 */
		Predicate<BlockPos> safe = pos-> {
			return (pos.getDistance(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()) < 128) && (Math.abs(this.getPos().getY() - pos.getY()) <= 3);
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
		//CircuitTileEntity circuitEntity = (CircuitTileEntity)getWorld().getTileEntity(candidatePos.get());
		if (candidatePos.isPresent()) {
			BlockPos position = candidatePos.get();
			Stream<BlockFace> faces = PosUtils.faces(position);
			List<BlockFace> faceList = faces.collect(Collectors.toList());
			BusSegment maximumSegment = new BusSegment(0);
			int maxWidth = 0;
			//Find the face with the largest bus width.  This is our input.
			for (BlockFace face : faceList) {
				Optional<BusSegment> currentSegment = CircuitBlock.getBusSegmentAt(getWorld(), face);
				if (currentSegment.isPresent()) {
				BusSegment segment = currentSegment.get();
				if (segment.getWidth() > maxWidth) {
					maxWidth = segment.getWidth();
					maximumSegment = segment;
				}
			}
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
