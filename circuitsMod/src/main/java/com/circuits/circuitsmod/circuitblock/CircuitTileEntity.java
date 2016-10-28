package com.circuits.circuitsmod.circuitblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitUID;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class CircuitTileEntity extends TileEntity {

	public Optional<BusSegment> getFaceSegment(EnumFacing face) {
		return null;
	}
	
	public CircuitUID getCircuitUID() {
		//TODO: Implement me!
		return null;
	}
	
	private final String name = "circuittileentity";
	
	//The circuit's name is the __only__ thing saved with this tile entity
	//The facing direction will be stored in block metadata
	private String circuitName = null;
	
	private BitInvoker impl = null;
	int numInputs = 0;
	int numOutputs = 0;
	
	
	//Parallel array with assigned input faces
	EnumFacing[] inputFaces = null;
	EnumFacing[] outputFaces = null;
	
	EnumFacing parentFacing = null;
	
	//Indexed by EnumFacing's numerical value, yields index into outputStates
	int[] outputIndices = null;
	
	
	boolean[] inputStates = null;
	boolean[] outputStates = null;
	
	private final int NOT_CONNECTED = -1;
	

	public void init(World worldIn, String circuitName) {
		
		if (!worldIn.isRemote && circuitName == null) {
			Microchips.ensureServerModelInit();
			setCircuit(circuitName);
			//TODO: Do we need to defer this?
			IBlockState state = worldIn.getBlockState(getPos());
			update(state);
		}
		else if (worldIn.isRemote) {
			this.circuitName = circuitName;
		}
		
	}
	
	public String getCircuitName() {
		return circuitName;
	}
	
	public EnumFacing getParentFacing() {
		if (this.parentFacing == null) {
			IBlockState parentState = getWorld().getBlockState(getPos());
			this.parentFacing = (EnumFacing)parentState.getValue(BlockDirectional.FACING);
		}
		return parentFacing;
	}
	
	private void generateDefaultFacing(EnumFacing parentFacing) {
		//If there's one input, have it be in the opposite direction
		switch (numInputs) {
		case 1:
			inputFaces[0] = parentFacing.getOpposite();
			switch (numOutputs) {
			case 1:
				outputFaces[0] = parentFacing;
				break;
			case 2:
				outputFaces[0] = parentFacing.rotateYCCW();
				outputFaces[1] = parentFacing.rotateY();
			case 3:
				outputFaces[0] = parentFacing.rotateYCCW();
				outputFaces[1] = parentFacing;
				outputFaces[2] = parentFacing.rotateY();
			}
			break;
		case 2:
			switch (numOutputs) {
			case 1:
				inputFaces[0] = parentFacing.rotateYCCW();
				inputFaces[1] = parentFacing.rotateY();
				outputFaces[0] = parentFacing;
				break;
			case 2:
				inputFaces[0] = parentFacing.rotateYCCW();
				inputFaces[1] = parentFacing.getOpposite();
				outputFaces[0] = parentFacing;
				outputFaces[1] = parentFacing.rotateY();
			}
			break;
		}
		
		for (int i = 0; i < outputIndices.length; i++) {
			outputIndices[i] = NOT_CONNECTED;
		}
		for (int i = 0; i < outputFaces.length; i++) {
			outputIndices[outputFaces[i].getIndex()] = i;
		}
	}
	
	private void setCircuit(String circuitName) {
		this.circuitName = circuitName;
		impl = Microchips.implMap.get(circuitName).impl;
		numInputs = impl.inputNames().length;
		numOutputs = impl.outputNames().length;
		
		inputFaces = new EnumFacing[numInputs];
		outputFaces = new EnumFacing[numOutputs];
		
		
		outputIndices = new int[EnumFacing.VALUES.length];
		 
		inputStates = new boolean[numInputs];
		outputStates = new boolean[numOutputs];
		
		//EnumFacing parentFacing = EnumFacing.DOWN;
		generateDefaultFacing(getParentFacing());
	}
	
	boolean isSidePowered(EnumFacing side) {
		BlockPos pos = getPos().offset(side);
		if (getWorld().getRedstonePower(pos, side) > 0) {
			return true;
		}
		else {
			IBlockState iblockstate1 = getWorld().getBlockState(pos);
			return iblockstate1.getBlock() == Blocks.redstone_wire && ((Integer)iblockstate1.getValue(BlockRedstoneWire.POWER)).intValue() > 0;
		}
	}
	
	private void notifyNeighbor(EnumFacing side) {
        BlockPos blockpos1 = pos.offset(side);
        if(net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(getWorld(), pos, getWorld().getBlockState(pos), java.util.EnumSet.of(side)).isCanceled())
            return;
        getWorld().notifyBlockOfStateChange(blockpos1, this.blockType);
        getWorld().notifyNeighborsOfStateExcept(blockpos1, this.blockType, side.getOpposite());
	}
	
	public void update(IBlockState state) {
		if (impl == null) {
			if (getWorld() != null && !getWorld().isRemote && circuitName == null) {
				Microchips.ensureServerModelInit();
				setCircuit("Or");
				update(state);
			}
			//TODO: Synching is going to mean just synching the name
			else if (getWorld().isRemote) {
				circuitName = "Or";
			}
		}
		else {
			System.out.println("Updating circuit tile entity!");
			for (int i = 0; i < numInputs; i++) {
				if (inputFaces[i] != null) {
					inputStates[i] = isSidePowered(inputFaces[i]);
					System.out.println(inputStates[i]);
				}
			}
			System.out.println(Arrays.asList(inputStates).stream().collect(Collectors.toCollection(ArrayList::new)));
			outputStates = impl.compute(inputStates);
			
			for (EnumFacing side : outputFaces) {
				notifyNeighbor(side);
			}
			
			System.out.println(Arrays.asList(outputStates).stream().collect(Collectors.toCollection(ArrayList::new)));
			getWorld().notifyNeighborsOfStateChange(getPos(), blockType);
		}
	}

	public int isProvidingWeakPower(IBlockState state, EnumFacing side) {
		if (impl != null) {
			int outputIndex = outputIndices[side.getIndex()];
			if (outputIndex != NOT_CONNECTED) {
				return outputStates[outputIndex] ? 15 : 0;
			}
		}
		
		return 0;
	}
}
