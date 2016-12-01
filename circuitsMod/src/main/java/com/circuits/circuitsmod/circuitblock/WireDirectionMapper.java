package com.circuits.circuitsmod.circuitblock;

import java.io.Serializable;
import java.util.Optional;

import net.minecraft.util.EnumFacing;

/**
 * Class representing the mapping between input/output indices
 * w.r.t. a Circuit Invoker and the direction (EnumFacing)
 * of that input/output face on a given Circuit Tile Entity
 * @author bubble-07
 *
 */
public class WireDirectionMapper implements Serializable {
	
	public static abstract class WireDirectionGenerator implements Serializable {
		private static final long serialVersionUID = 1L;

		public abstract WireDirectionMapper getMapper(EnumFacing parentFacing, int numInputs, int numOutputs);
	}

	private static final long serialVersionUID = 1L;

	private WireDirectionMapper(EnumFacing[] inputFaces, EnumFacing[] outputFaces) {
		this.inputFaces = inputFaces;
		this.outputFaces = outputFaces;
	}
	private final EnumFacing[] inputFaces;
	private final EnumFacing[] outputFaces;
	
	/**
	 * A default, hard-coded wire direction generator for when
	 * the creator of a circuit fails to specify one.
	 */
	public static class DefaultGenerator extends WireDirectionGenerator {
		private static final long serialVersionUID = 1L;

		@Override
		public WireDirectionMapper getMapper(EnumFacing parentFacing, int numInputs, int numOutputs) {
			EnumFacing[] inputFaces = new EnumFacing[numInputs];
			EnumFacing[] outputFaces = new EnumFacing[numOutputs];
			
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
					break;
				case 3:
					outputFaces[0] = parentFacing.rotateYCCW();
					outputFaces[1] = parentFacing;
					outputFaces[2] = parentFacing.rotateY();
					break;
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
					inputFaces[0] = parentFacing.getOpposite();
					inputFaces[1] = parentFacing.rotateY();
					outputFaces[0] = parentFacing.rotateYCCW();
					outputFaces[1] = parentFacing;
					break;
				}
				break;
			case 3:
				inputFaces[0] = parentFacing.rotateYCCW();
				inputFaces[1] = parentFacing.getOpposite();
				inputFaces[2] = parentFacing.rotateY();
				outputFaces[0] = parentFacing;
			}
			return new WireDirectionMapper(inputFaces, outputFaces);
		}
	}
	
	
	private Optional<Integer> search(EnumFacing facing, EnumFacing[] array) {
		for (int i = 0; i < array.length; i++) {
			if (facing.equals(array[i])) {
				return Optional.of(i);
			}
		}
		return Optional.empty();
	}
	
	public Optional<Integer> getInputIndexOf(EnumFacing facing) {
		return search(facing, inputFaces);
	}
	public Optional<Integer> getOutputIndexOf(EnumFacing facing) {
		return search(facing, outputFaces);
	}
	public EnumFacing getInputFace(int index) {
		return inputFaces[index];
	}
	public EnumFacing getOutputFace(int index) {
		return outputFaces[index];
	}
	
	public EnumFacing[] getOutputFaces() {
		return this.outputFaces;
	}
	public EnumFacing[] getInputfaces() {
		return this.inputFaces;
	}
	
}
