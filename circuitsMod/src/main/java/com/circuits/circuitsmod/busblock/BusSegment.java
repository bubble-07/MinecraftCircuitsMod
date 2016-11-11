package com.circuits.circuitsmod.busblock;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.Log;

public class BusSegment {
	private Set<BlockFace> inputs = new HashSet<BlockFace>();
	private Set<BlockFace> outputs = new HashSet<BlockFace>();
	private BusData currentVal;
	private int busWidth;
	private int numWaiting = 0;
	
	public BusSegment(int busWidth) {
		this.busWidth = busWidth;
		this.currentVal = new BusData(busWidth, 0L);
	}
	
	public int getWidth() {
		return this.busWidth;
	}
	
	/**
	 * Mutating in-place operation which takes another bus segment as input, pulls
	 * all of the input and output faces from the other bus segment and adds them
	 * to this one, and then replaces the references to the bus segments
	 * within the circuit tile entities of the other to point at this one instead
	 * @param worldIn
	 * @param other
	 */
	public void unifyWith(World worldIn, BusSegment other) {
		
		//TODO: What happens if we unify as signal is flowing?
		inputs.addAll(other.inputs);
		outputs.addAll(other.outputs);
		for (BlockFace inFace : other.inputs) {
			CircuitBlock.setBusSegmentAt(worldIn, inFace, this);
		}
		for (BlockFace outFace : other.outputs) {
			CircuitBlock.setBusSegmentAt(worldIn, outFace, this);
		}
	}
	
	public void removeAllAt(BlockPos pos) {
		this.inputs.removeIf((f) -> f.getPos().equals(pos));
		this.outputs.removeIf((f) -> f.getPos().equals(pos));
	}
	
	public void addInput(BlockFace inputFace) {
		inputs.add(inputFace);
	}
	public void removeInput(BlockFace inputFace) {
		inputs.remove(inputFace);
	}
	public void addOutput(BlockFace outputFace) {
		outputs.add(outputFace);
	}
	public void removeOutput(BlockFace outputFace) {
		outputs.remove(outputFace);
	}
	
	/**
	 * The main responsibility of a BusSegment: connected
	 * inputs will accumulate BusData into this segment's internal register,
	 * but once all input faces have finished accumulating their data for the tick,
	 * this BusSegment must deliver signals to all circuit tile entities on the output faces.
	 * @param other
	 */
	public void accumulate(World worldIn, BusData other) {
		if (other.getWidth() != busWidth) {
			Log.internalError("ERROR: Attempting to accumulate a value of width: " + other.getWidth() + 
					          " in BusSegment " + this.toString());
			return;
		}
		this.currentVal = this.currentVal.combine(other);
		numWaiting--;
		if (numWaiting == 0) {
			//Oh boy! Time to actually do stuff!
			numWaiting = inputs.size();
			
			for (BlockFace face : this.outputs) {
				Optional<CircuitTileEntity> circuitEntity = CircuitBlock.getCircuitTileEntityAt(worldIn, face.getPos());
				if (!circuitEntity.isPresent()) {
					continue;
				}
				circuitEntity.get().receiveInput(face.getFacing(), this.currentVal);
			}
		}
	}
	
	
	
	public String toString() {
		return "BusSegment[in=" + inputs.toString() + ", out=" + outputs.toString() + 
				           ", val=" + currentVal.toString() + "]";
	}
	
}
