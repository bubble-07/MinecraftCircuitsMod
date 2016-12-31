package com.circuits.circuitsmod.busblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.circuitblock.StartupCommonCircuitBlock;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.Log;

public class BusSegment {
	private Set<BlockFace> inputs = new HashSet<BlockFace>();
	private Set<BlockFace> outputs = new HashSet<BlockFace>();
	private HashMap<BlockFace, BusData> inputData = new HashMap<>();
	private BusData currentVal;
	private int busWidth;
	
	public BusSegment(int busWidth) {
		this.busWidth = busWidth;
		this.currentVal = new BusData(busWidth, 0L);
	}
	
	public int getWidth() {
		return this.busWidth;
	}
	
	public BusData getCurrentVal() {
		return currentVal;
	}
	
	/**
	 * Split off a bus segment from this one with the same
	 * bus width, but with the
	 * inputs and outputs restricted to the set passed in to this method.
	 * @param face
	 * @return
	 */
	public BusSegment splitOff(Set<BlockFace> faces) {
		BusSegment result = new BusSegment(busWidth);
		for (BlockFace face : faces) {
			if (inputs.contains(face)) {
				result.addInput(face);
			}
			if (outputs.contains(face)) {
				result.addOutput(face);
			}
		}
		result.currentVal = this.currentVal.copy();
		result.inputData = new HashMap<>();
		for (BlockFace inputFace : result.inputs) {
			result.inputData.put(inputFace, this.inputData.get(inputFace));
		}
		return result;
	}
	
	/**
	 * Mutating in-place operation which takes another bus segment as input, pulls
	 * all of the input and output faces from the other bus segment and adds them
	 * to this one, and then replaces the references to the bus segments
	 * within the circuit tile entities of the other to point at this one instead.
	 * 
	 * If this operation is called as part of a chain of unifications, make sure to
	 * call "forceUpdate" afterwards to propagate the merged waiting input signals
	 * by forcing an update to the current output value!
	 * @param worldIn
	 * @param other
	 */
	public void unifyWith(World worldIn, BusSegment other) {
		inputs.addAll(other.inputs);
		outputs.addAll(other.outputs);
		for (Entry<BlockFace, BusData> otherData : other.inputData.entrySet()) {
			this.inputData.put(otherData.getKey(), otherData.getValue());
		}
		
		for (BlockFace inFace : other.inputs) {
			CircuitBlock.setBusSegmentAt(worldIn, inFace, this);
		}
		for (BlockFace outFace : other.outputs) {
			CircuitBlock.setBusSegmentAt(worldIn, outFace, this);
		}
	}
	
	private void pushOutputSignals(IBlockAccess worldIn) {
		for (BlockFace face : this.outputs) {
			Optional<CircuitTileEntity> circuitEntity = CircuitBlock.getCircuitTileEntityAt(worldIn, face.getPos());
			if (!circuitEntity.isPresent()) {
				continue;
			}
			circuitEntity.get().receiveInput(face.getFacing(), this.currentVal);
		}
	}
	
	private BusData computeOutput() {
		BusData result = new BusData(busWidth, 0L);
		for (BusData input : this.inputData.values()) {
			result = result.or(input);
		}
		return result;
	}
	
	private void updateIfChanged(IBlockAccess worldIn) {
		BusData newVal = computeOutput();
		if (!newVal.equals(currentVal)) {
			this.currentVal = newVal;
			pushOutputSignals(worldIn);
		}
	}
	
	public void forceUpdate(IBlockAccess worldIn) {
		this.currentVal = computeOutput();
		pushOutputSignals(worldIn);
	}
	
	public void removeAllAt(BlockPos pos) {
		this.inputs.removeIf((f) -> f.getPos().equals(pos));
		this.outputs.removeIf((f) -> f.getPos().equals(pos));
		Set<BlockFace> datasToRemove = this.inputData.keySet().stream().filter((f) -> f.getPos().equals(pos)).collect(Collectors.toSet());
		for (BlockFace face : datasToRemove) {
			this.inputData.remove(face);
		}
	}
	
	public void addInput(BlockFace inputFace) {
		inputs.add(inputFace);
		this.inputData.put(inputFace, new BusData(busWidth, 0L));
	}
	public void removeInput(BlockFace inputFace) {
		inputs.remove(inputFace);
		this.inputData.remove(inputFace);
	}
	public void addOutput(BlockFace outputFace) {
		outputs.add(outputFace);
	}
	public void removeOutput(BlockFace outputFace) {
		outputs.remove(outputFace);
	}
	
	/**
	 * The main responsibility of a BusSegment: connected
	 * inputs will accumulate BusData into this segment's internal map of inputs,
	 * and if any signal has changed, this will force an update on all of the output faces
	 * @param other
	 */
	public void accumulate(World worldIn, BlockFace inputFace, BusData newInputVal) {
		
		if (newInputVal.getWidth() != busWidth) {
			Log.internalError("ERROR: Attempting to accumulate a value of width: " + newInputVal.getWidth() + 
					          " in BusSegment " + this.toString());
			return;
		}
		if (!inputData.containsKey(inputFace)) {
			Log.internalError("WARN: Attempting to accumulate a value from a face we're not expecting");
			return;
		}
		
		BusData oldInputVal = this.inputData.get(inputFace);
		if (oldInputVal.equals(newInputVal)) {
			//Do nothing!
			return;
		}
		
		this.inputData.put(inputFace, newInputVal);
		forceUpdate(worldIn);

	}
	
	
	
	public String toString() {
		return "BusSegment[in=" + inputs.toString() + ", out=" + outputs.toString() + 
				           ", val=" + currentVal.toString() + "]";
	}
	
}
