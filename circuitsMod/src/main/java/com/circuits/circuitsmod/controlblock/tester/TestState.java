package com.circuits.circuitsmod.controlblock.tester;

import java.io.Serializable;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;

//Testing state, as seen by the tile entity
public class TestState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int testindex;
	public int numTests;
	public SpecializedCircuitUID circuitUID;
	public TestConfig config;
	public boolean finished;
	public boolean success;
	public TestState(SpecializedCircuitUID circuitUID, int testindex, int numTests, boolean finished, boolean success, TestConfig config) {
		this.circuitUID = circuitUID;
		this.testindex = testindex;
		this.numTests = numTests;
		this.config = config;
		this.finished = finished;
		this.success = success;
	}
}
