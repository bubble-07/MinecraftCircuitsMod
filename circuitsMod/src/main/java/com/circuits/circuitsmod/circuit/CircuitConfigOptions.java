package com.circuits.circuitsmod.circuit;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Holds the configuration options for the specialization of a given circuit
 * For now, this is just an array of integers
 * @author bubble-07
 *
 */
public class CircuitConfigOptions implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final int[] opts;
	
	public CircuitConfigOptions() {
		this.opts = new int[0];
	}
	public CircuitConfigOptions(int[] opts) {
		this.opts = opts;
	}
	public int[] asInts() {
		return opts;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(opts);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CircuitConfigOptions other = (CircuitConfigOptions) obj;
		if (!Arrays.equals(opts, other.opts))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "CircuitConfigOptions [opts=" + Arrays.toString(opts) + "]";
	}
	
}

